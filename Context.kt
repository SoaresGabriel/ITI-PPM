class Context(
        val symbol: Int,
        val order: Int,
        val vine: Context?,
        private val sibling: Context? = null
) {

    private var child: Context? = null

    private var count: Int = 1

    val isRoot: Boolean get() = order == -1

    fun newChild(symbol: Int, vine: Context): Context {
        val newChild = Context(symbol, this.order + 1, vine, sibling = this.child)
        this.child = newChild
        return newChild
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
    fun getFrequenciesAndSymbolContext(symbol: Int, removeSymbol: BooleanArray): FrequenciesAndContext {

        val frequencies = mutableListOf<Int>()

        var wantedContext: Context? = null
        var wantedIndex: Int = frequencies.lastIndex

        if(this.child != null) {
            var c = 0
            var currentContext = this.child!!
            while(true) {
                if(!removeSymbol[currentContext.symbol]) {
                    removeSymbol[currentContext.symbol] = true

                    if(currentContext.symbol == symbol) { // symbol found in childs
                        wantedContext = currentContext
                        wantedIndex = c
                    }

                    frequencies.add(currentContext.count)
                    c++
                }

                currentContext = currentContext.sibling ?: break
            }

            if(c > 0) frequencies.add(c) // add the escape count

            if (wantedContext == null) {
                wantedIndex = frequencies.lastIndex // escape index
            }
        }

        return FrequenciesAndContext(frequencies.toIntArray(), wantedContext, wantedIndex)
    }

    // used to return two results in the getFrequenciesAndSymbolContext function
    class FrequenciesAndContext(private val frequencies: IntArray, private val context: Context?, private val index: Int) {
        operator fun component1(): IntArray = frequencies
        operator fun component2(): Context? = context
        operator fun component3(): Int = index
    }

    fun getFrequenciesAndContexts(removeSymbol: BooleanArray): FrequenciesAndContexts {

        val frequencies = mutableListOf<Int>()
        val contexts = mutableListOf<Context>()


        if(this.child != null) {
            var c = 0

            var currentContext = this.child!!
            while(true) {
                if(!removeSymbol[currentContext.symbol]) {
                    removeSymbol[currentContext.symbol] = true

                    frequencies.add(currentContext.count)
                    contexts.add(currentContext)
                    c++
                }

                currentContext = currentContext.sibling ?: break
            }

            if(c > 0) frequencies.add(c) // add the escape count
        }

        return FrequenciesAndContexts(frequencies.toIntArray(), contexts)
    }

    class FrequenciesAndContexts(private val frequencies: IntArray, private val contexts: List<Context>) {
        operator fun component1(): IntArray = frequencies
        operator fun component2(): List<Context> = contexts
    }

}