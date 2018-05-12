import arithmeticEncoder.ArithmeticEncoder
import arithmeticEncoder.FlatFrequencyTable
import arithmeticEncoder.SimpleFrequencyTable

class PpmEncoder(
        private val encoder: ArithmeticEncoder,
        private val maxContextOrder: Int
) {
    private val remainingSymbols: MutableList<Int> = (0..255).toMutableList()

    private val seen = BooleanArray(256) { false }

    private var context = Context(-1, -1, 0)

    fun encode(symbol: Int) {
        context = encodeAux(context, symbol)

        if(!seen[symbol]) { // first time symbol appears
            encoder.write(FlatFrequencyTable(remainingSymbols.size), remainingSymbols.indexOf(symbol))
            remainingSymbols.remove(symbol)
            seen[symbol] = true
        }
    }

    private fun encodeAux(context: Context, symbol: Int): Context {
        if(context.order == maxContextOrder) { // cant create a context with higher order, go to shorter context
            return encodeAux(context.vine!!, symbol)
        }

        if(context.child == null) { // this context has no childs, create the child with this symbol
            context.child = Context(symbol, context.order + 1, 0)
            context.childCount++

            context.child!!.vine = if(context.isRoot) context else encodeAux(context.vine!!, symbol)

            return context.child!!
        }

        val (frequencies, symbolContext) = context.getFrequenciesAndSymbolContext(symbol)

        return if(symbolContext.symbol == symbol) { // the symbol is already a child of that context
            encoder.write(SimpleFrequencyTable(frequencies), symbolContext.index)

            symbolContext.increment()
            symbolContext
        } else {
            encoder.write(SimpleFrequencyTable(frequencies), context.escapeIndex)

            symbolContext.sibling = Context(symbol, symbolContext.order, symbolContext.index + 1)
            context.childCount++

            symbolContext.sibling!!.vine = if(context.isRoot) context else encodeAux(context.vine!!, symbol)

            symbolContext.sibling!!
        }
    }
}