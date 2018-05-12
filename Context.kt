class Context(
        val symbol: Int,
        val order: Int,
        val index: Int,
        var vine: Context? = null
) {

    var child: Context? = null

    var sibling: Context? = null

    private var count: Int = 1

    var childCount: Int = 0

    val isRoot: Boolean get() = order == -1

    val escapeIndex get() = childCount

    /**
     * Increment this context occurrence and propagates through the low order context
     * */
    fun increment() {
        this.count++
        if(!vine!!.isRoot) vine!!.increment()
    }

    /**
     * Iterate through the children, get the frequencies, and look for the symbol
     * @param symbol wanted symbol
     * @return the array of frequencies and the context of the symbol if its been found,
     * or the last context otherwise.
     * */
    fun getFrequenciesAndSymbolContext(symbol: Int): FrequenciesAndContext {

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

    // used to return two results in the getFrequenciesAndSymbolContext function
    class FrequenciesAndContext(private val frequencies: IntArray, private val context: Context) {
        operator fun component1(): IntArray = frequencies
        operator fun component2(): Context = context
    }

}