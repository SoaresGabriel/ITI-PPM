import arithmeticEncoder.ArithmeticEncoder
import arithmeticEncoder.FlatFrequencyTable

class PpmEncoder(
        private val encoder: ArithmeticEncoder,
        contextOrder: Int
) {
    private val remainingSymbols: MutableList<Int> = (0..255).toMutableList()

    private val seen = BooleanArray(256) { false }

    init { maxContextOrder = contextOrder }

    private var context = Context(-1, 0, 0)

    fun encode(symbol: Int) {
        context = context.encode(encoder, symbol)

        if(!seen[symbol]) { // first time symbol appears
            encoder.write(FlatFrequencyTable(remainingSymbols.size), remainingSymbols.indexOf(symbol).takeIf { it >= 0 }!!)
            remainingSymbols.remove(symbol)
            seen[symbol] = true
        }
    }
}