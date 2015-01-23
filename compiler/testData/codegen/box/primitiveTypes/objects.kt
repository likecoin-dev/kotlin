fun box(): String {
    try {
        // Objects
        val i = Int
        val d = Double

        test(Int)
        test(Double)

        // Common Double
        Double.POSITIVE_INFINITY
        Double.NEGATIVE_INFINITY
        Double.NaN

        // Parsing
        if (Int.parse("12") != 12 || Double.parse("1.01") != 1.01) {
            return "Wrong parse result"
        }
    }
    catch (e: Throwable) {
        return "Error: \n" + e
    }

    return "OK"
}

fun test(a: Any) {

}


