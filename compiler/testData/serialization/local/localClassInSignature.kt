// CLASS_NAME_SUFFIX: $main$Local

fun main() {
    open class Local {
        fun param(l: Local) {}

        val returnType: Local = this

        fun both(l: Local) = l

        fun <T : Local, U : T> generic(t: T): U = null!!
    }
}
