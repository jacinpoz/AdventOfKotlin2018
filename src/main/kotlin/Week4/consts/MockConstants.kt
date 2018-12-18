package Week4.consts

val HASH_CODE_METHOD = Object::class.java.getDeclaredMethod("hashCode")
val TO_STRING_METHOD = Object::class.java.getDeclaredMethod("toString")
val EQUALS_METHOD = Object::class.java.getDeclaredMethod("equals", Object::class.java)
val PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES = mapOf<Class<*>, Any>(
        Boolean::class.java to false,
        Char::class.java to '\u0000',
        Byte::class.java to 0.toByte(),
        Short::class.java to 0.toShort(),
        Int::class.java to 0,
        Long::class.java to 0L,
        Float::class.java to 0F,
        Double::class.java to 0.0,

        java.lang.Boolean::class.java to false,
        java.lang.Character::class.java to '\u0000',
        java.lang.Byte::class.java to 0.toByte(),
        java.lang.Short::class.java to 0.toShort(),
        java.lang.Integer::class.java to 0,
        java.lang.Long::class.java to 0L,
        java.lang.Float::class.java to 0F,
        java.lang.Double::class.java to 0.0
)
