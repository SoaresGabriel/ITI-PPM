import arithmeticEncoder.ArithmeticDecoder
import arithmeticEncoder.FlatFrequencyTable
import arithmeticEncoder.SimpleFrequencyTable

class PpmDecoder(
    private val decoder: ArithmeticDecoder,
    private val maxContextOrder: Int
) {
    private val remainingSymbols: MutableList<Int> = (0..256).toMutableList()

    private var context = Context(-1, -1, null)

    fun decode(): Int {
        context = decodeAux(context, BooleanArray(256))
        return context.symbol
    }

    private fun decodeAux(context: Context, removeSymbol: BooleanArray): Context {
        if(context.order == maxContextOrder) { // cant create a context with higher order, go to shorter context
            return decodeAux(context.vine!!, removeSymbol)
        }

        val (frequencies, contexts) = context.getFrequenciesAndContexts(removeSymbol)

        val index = if (frequencies.isNotEmpty()){
            decoder.read(SimpleFrequencyTable(frequencies)) // decode if this context has children
        } else frequencies.lastIndex

        return if(index == frequencies.lastIndex) { // escape index

            if(context.isRoot) { // new symbol
                val symbol = readNewSymbol()
                context.newChild(symbol, context)
            } else {
                val vine = decodeAux(context.vine!!, removeSymbol)
                context.newChild(vine.symbol, vine)
            }
        } else {
            contexts[index].apply { increment() }
        }
    }

    private fun readNewSymbol(): Int {
        val frequencyTable = FlatFrequencyTable(remainingSymbols.size)
        val index = decoder.read(frequencyTable)

        val symbol = remainingSymbols[index]
        remainingSymbols.removeAt(index)

        return symbol
    }

}