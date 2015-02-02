// CLASS_NAME_SUFFIX: Deepest

fun main() {
    class Local {
        inner class Inner {
            val prop = object {
                fun foo() {
                    fun bar() {
                        class DeepLocal {
                            inner class Deepest {
                                fun local() = Local()
                                fun inner() = Inner()
                                fun prop() = prop
                                fun deep() = DeepLocal()
                                fun deepest() = Deepest()
                            }
                        }
                    }
                }
            }
        }
    }
}
