class Context(
        val symbol: Int,
        val order: Int,
        val vine: Context?,
        private val sibling: Context? = null
) {

    private var child: Context? = null

    private var count: Int = 1

    private var childCount: Int = 0

    val isRoot: Boolean get() = order == -1

    fun newChild(symbol: Int, vine: Context): Context {
        val newChild = Context(symbol, this.order + 1, vine, sibling = this.child)
        this.child = newChild
        childCount++
        return newChild
    }

    fun getChildAt(index: Int): Context {
        var current = child!!
        for(i in 0 until index) {
            current = current.sibling!!
        }
        return current
    }

    /**
     * Increment this context occurrence and propagates through the low order context
     * */
    fun increment() {
        this.count++
        if(!vine!!.isRoot) vine.increment()
    }

    /**
     * Iterate through the children, get the frequencies, and look for the symbol
     * @param symbol wanted symbol
     * @return the array of frequencies and the context of the symbol if its been found,
     * or the last context otherwise.
     * */
    fun getFrequenciesAndSymbolContext(symbol: Int): FrequenciesAndContext {

        if(this.child == null) {
            return FrequenciesAndContext(IntArray(0), null, 0)
        }

        val frequencies = IntArray(childCount + 1)

        var wantedContext: Context? = null
        var wantedIndex: Int = -1

        var c = 0
        var currentContext = this.child!!
        while(true) {
            if(currentContext.symbol == symbol) { // symbol found in childs
                wantedContext = currentContext
                wantedIndex = c
            }

            frequencies[c++] = currentContext.count

            // current is the last element when while breaks
            currentContext = currentContext.sibling ?: break
        }

        frequencies[c] = childCount // add the escape count

        if (wantedContext == null) {
            wantedIndex = frequencies.lastIndex // escape index
        }

        return FrequenciesAndContext(frequencies, wantedContext, wantedIndex)
    }

    fun getFrequencies(): IntArray = getFrequenciesAndSymbolContext(-1).component1()

    // used to return two results in the getFrequenciesAndSymbolContext function
    class FrequenciesAndContext(private val frequencies: IntArray, private val context: Context?, private val index: Int) {
        operator fun component1(): IntArray = frequencies
        operator fun component2(): Context? = context
        operator fun component3(): Int = index
    }

}