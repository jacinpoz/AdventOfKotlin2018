package Week4.core

// Could be extended with extra types if needed
sealed class MockType {
    abstract fun score(any: Any?): Int

    object AnyType : MockType() {
        override fun score(any: Any?) = 0
    }

    class EqualType(private val any: Any?) : MockType() {
        override fun score(any: Any?): Int = if (this.any == any) 1 else -1
    }

    object NullType : MockType() {
        override fun score(any: Any?): Int = if (any == null) 1 else -1
    }
}