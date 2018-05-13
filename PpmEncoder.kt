import arithmeticEncoder.ArithmeticEncoder
import arithmeticEncoder.FlatFrequencyTable
import arithmeticEncoder.SimpleFrequencyTable

class PpmEncoder(
        private val encoder: ArithmeticEncoder,
        private val maxContextOrder: Int
) {
    private val remainingSymbols: MutableList<Int> = (0..256).toMutableList()

    private var context = Context(-1, -1, 0, null)

    fun encode(symbol: Int) {
        context = encodeAux(context, symbol)
    }

    private fun encodeAux(context: Context, symbol: Int): Context {
        if(context.order == maxContextOrder) { // cant create a context with higher order, go to shorter context
            return encodeAux(context.vine!!, symbol)
        }

        if(context.child == null) { // this context has no children, create the child with this symbol

            val vine = if(context.isRoot) {
                writeNewSymbol(symbol)
                context
            } else encodeAux(context.vine!!, symbol)

            context.child = Context(symbol, context.order + 1, 0, vine)
            context.childCount++

            return context.child!!
        }

        val (frequencies, symbolContext) = context.getFrequenciesAndSymbolContext(symbol)

        return if(symbolContext.symbol == symbol) { // the symbol is already a child of that context
            encoder.write(SimpleFrequencyTable(frequencies), symbolContext.index)

            symbolContext.increment()
            symbolContext
        } else {
            encoder.write(SimpleFrequencyTable(frequencies), context.escapeIndex)

            val vine = if(context.isRoot) {
                writeNewSymbol(symbol)
                context
            } else encodeAux(context.vine!!, symbol)

            symbolContext.sibling = Context(symbol, symbolContext.order, symbolContext.index + 1, vine)
            context.childCount++

            symbolContext.sibling!!
        }
    }

    private fun writeNewSymbol(symbol: Int) {
        encoder.write(FlatFrequencyTable(remainingSymbols.size), remainingSymbols.indexOf(symbol))
        remainingSymbols.remove(symbol)
    }
}