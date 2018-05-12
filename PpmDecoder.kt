import arithmeticEncoder.ArithmeticDecoder
import arithmeticEncoder.FlatFrequencyTable
import arithmeticEncoder.SimpleFrequencyTable

class PpmDecoder(
    private val decoder: ArithmeticDecoder,
    private val maxContextOrder: Int
) {
    private val remainingSymbols: MutableList<Int> = (0..255).toMutableList()

    private var context = Context(-1, -1, 0)

    fun decode(): Int {

        context = decodeAux(context)
        return context.symbol

    }

    private fun decodeAux(context: Context): Context {
        if(context.order == maxContextOrder) {
            return decodeAux(context.vine!!)
        }

        if(context.isRoot && context.child == null){ // first call
            val symbol = readNewSymbol()

            context.child = Context(symbol, context.order + 1, 0)
            context.childCount++
            context.child!!.vine = context

            return context.child!!
        }

        if(context.child == null) {
            val vine = decodeAux(context.vine!!)
            context.child = Context(vine.symbol, context.order + 1, 0)
            context.childCount++
            context.child!!.vine = vine
            return context.child!!
        }

        val (frequencies, lastChild) = context.getFrequenciesAndSymbolContext(-1)

        val index = decoder.read(SimpleFrequencyTable(frequencies))

        if(index == context.escapeIndex) {

            val newChild = if(context.isRoot) { // new symbol
                val symbol = readNewSymbol()
                Context(symbol, context.order + 1, lastChild.index + 1, vine = context)
            } else {
                val vine = decodeAux(context.vine!!)
                Context(vine.symbol, context.order + 1, lastChild.index + 1, vine)
            }

            lastChild.sibling = newChild
            context.childCount++

            return newChild
        } else {
            var current = context.child!!
            for(i in 0 until index) {
                current = current.sibling!!
            }
            current.increment()
            return current
        }
    }

    private fun readNewSymbol(): Int {
        val frequencyTable = FlatFrequencyTable(remainingSymbols.size)
        val index = decoder.read(frequencyTable)

        val symbol = remainingSymbols[index]
        remainingSymbols.remove(symbol)

        return symbol
    }

}