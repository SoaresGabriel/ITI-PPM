import arithmeticEncoder.ArithmeticEncoder
import arithmeticEncoder.SimpleFrequencyTable

var maxContextOrder: Int = 0

class Context(
        val symbol: Int,
        private val order: Int,
        private val index: Int
) {

    var vine: Context? = null

    var child: Context? = null

    var sibling: Context? = null

    var count: Int = 1

    var childCount: Int = 0

    fun encode(encoder: ArithmeticEncoder, symbol: Int): Context {

        if(order == maxContextOrder) { // cant create a context with higher order, go to shorter context
            return vine!!.encode(encoder, symbol)
        }

        if(child == null) { // this context has no childs, create the child with this symbol
            this.child = Context(symbol,  order + 1, 0)
            childCount++
            if(this.isRoot) {
                this.child!!.vine = this
            } else {
                this.child!!.vine = vine!!.encode(encoder, symbol)
            }
            return this.child!!
        }

        val (frequencies, context) = getFrequenciesAndSymbolContext(symbol)

        encoder.write(SimpleFrequencyTable(frequencies), index)

        return if(context.symbol == symbol) { // the symbol is already a child of that context
            context.increment()
            context

        } else {
            context.sibling = Context(symbol, context.order, 0)
            childCount++

            if(isRoot) {
                context.sibling!!.vine = this
            } else {
                context.sibling!!.vine = vine!!.encode(encoder, symbol)
            }

            context.sibling!!
        }

    }

    /**
     * Iterate through the children, get the frequencies, and look for the symbol
     * @param symbol wanted symbol
     * @return the array of frequencies and the context of the symbol if its been found,
     * or the last context otherwise.
     * */
    private fun getFrequenciesAndSymbolContext(symbol: Int): FrequenciesAndContext {

        val hasEscape = childCount < 256
        val frequencies = IntArray(if(hasEscape) childCount + 1 else 256)
        var wantedContext: Context? = null

        var currentContext = this.child!!
        while(true) {
            if(currentContext.symbol == symbol) { // symbol found in childs
                wantedContext = currentContext
            }

            frequencies[currentContext.index] = currentContext.count

            // current is the last element when while breaks
            currentContext.sibling?.let { currentContext = it } ?: break
        }

        if(hasEscape) { // add the escape count
            frequencies[escapeIndex] = childCount
        }

        if(!hasEscape && wantedContext == null) throw IllegalStateException("This context has $childCount childs but no $symbol")

        if (wantedContext == null) {
            wantedContext = currentContext
        }

        return FrequenciesAndContext(frequencies, wantedContext)
    }

    private class FrequenciesAndContext(val frequencies: IntArray, val context: Context) {
        operator fun component1(): IntArray = frequencies
        operator fun component2(): Context = context
    }

    private fun increment() {
        this.count++
        if(!vine!!.isRoot) vine!!.increment()
    }

    val isRoot: Boolean get() = order == 0

    private val escapeIndex get() = childCount

}