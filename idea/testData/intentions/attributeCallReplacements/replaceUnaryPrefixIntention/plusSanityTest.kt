// INTENTION_TEXT: Replace with '+' prefix
fun test() {
    class Test {
        fun plus(): Test = Test()
    }
    val test = Test()
    test.pl<caret>us()
}
