import arithmeticEncoder.ArithmeticEncoder
import arithmeticEncoder.FlatFrequencyTable
import arithmeticEncoder.SimpleFrequencyTable

class PpmEncoder(
        private val encoder: ArithmeticEncoder,
        private val maxContextOrder: Int
) {
    private val remainingSymbols: MutableList<Int> = (0..256).toMutableList()

    private var context = Context(-1, -1, null)

    fun encode(symbol: Int) {
        context = encodeAux(context, symbol, BooleanArray(256))
    }

    private fun encodeAux(context: Context, symbol: Int, removeSymbol: BooleanArray): Context {
        if(context.order == maxContextOrder) { // cant create a context with higher order, go to shorter context
            return encodeAux(context.vine!!, symbol, removeSymbol)
        }

        val (frequencies, symbolContext, index) = context.getFrequenciesAndSymbolContext(symbol, removeSymbol)

        // encode if have children
        if(frequencies.isNotEmpty()) encoder.write(SimpleFrequencyTable(frequencies), index)

        return if(symbolContext == null) { // symbol not found in this context

            val vine = if(context.isRoot) {
                writeNewSymbol(symbol)
                context
            } else encodeAux(context.vine!!, symbol, removeSymbol)

            context.newChild(symbol, vine)
        } else {
            symbolContext.increment()
            symbolContext
        }
    }

    private fun writeNewSymbol(symbol: Int) {
        val index = remainingSymbols.indexOf(symbol)
        encoder.write(FlatFrequencyTable(remainingSymbols.size), index)
        remainingSymbols.removeAt(index)
    }
}