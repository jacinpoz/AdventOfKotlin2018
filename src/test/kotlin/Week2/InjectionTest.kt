package Week2

import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class InjectionTest {

    @Test
    fun `Basic default dependency injection`() {
        "TEST".bind()
        500.bind()
        val random = { Random(System.nanoTime()) }
        random.bind()
        class InjectionTest{
            val myGet: String = get()
            val myInt: Int = get()
            val myInject: String by inject()
            val random: Random = get()
        }
        val injectionTest = InjectionTest()
        assertEquals("TEST", injectionTest.myGet)
        assertEquals("TEST", injectionTest.myInject)
        assertEquals(500, injectionTest.myInt)
        val firstRandom = injectionTest.random
        val secondRandom = InjectionTest().random
        assertNotEquals(firstRandom, secondRandom)
    }

    @Test
    fun `Basic module dependency injection`() {
        "TEST".bind("UNIT_TEST")
        500.bind("UNIT_TEST")
        val random = { Random(System.nanoTime()) }
        random.bind("UNIT_TEST")
        class InjectionTest{
            val myGet: String = get("UNIT_TEST")
            val myInt: Int = get("UNIT_TEST")
            val defaultGet: String = get()
            val myInject: String by inject("UNIT_TEST")
            val random: Random = get("UNIT_TEST")
        }
        val injectionTest = InjectionTest()
        assertEquals("TEST", injectionTest.myGet)
        assertEquals("TEST", injectionTest.myInject)
        assertEquals(500, injectionTest.myInt)
        assertEquals("", injectionTest.defaultGet)
        val firstRandom = injectionTest.random
        val secondRandom = InjectionTest().random
        assertNotEquals(firstRandom, secondRandom)
    }

    @Test
    fun `DSL module dependency injection`() {
        val module1 = module("UNIT_TEST") {
            "TEST".bind()
            500.bind()
            val random = { Random(System.nanoTime()) }
            random.bind()
        }
        class InjectionTest(module: Module){
            val myGet: String = module.get()
            val myInt: Int = module.get()
            val defaultGet: String = get()
            val myInject: String by module.inject()
            val random: Random = module.get()
        }
        val injectionTest = InjectionTest(module1)
        assertEquals("TEST", injectionTest.myGet)
        assertEquals("TEST", injectionTest.myInject)
        assertEquals(500, injectionTest.myInt)
        assertEquals("", injectionTest.defaultGet)
        val firstRandom = injectionTest.random
        val secondRandom = InjectionTest(module1).random
        assertNotEquals(firstRandom, secondRandom)
    }

    @Test
    fun `Interface dependency injection`() {
        val charSequence : CharSequence = "MY_CHAR_SEQUENCE"
        charSequence.bind()
        class InjectionTest{
            val myString: String = get()
            val myCharSequence: CharSequence = get()
        }
        val injectionTest = InjectionTest()
        assertEquals("", injectionTest.myString)
        assertEquals("MY_CHAR_SEQUENCE", injectionTest.myCharSequence)
    }

    @Test
    fun `Interface dependency injection using diamond syntax`() {
        "MY_CHAR_SEQUENCE".bind<CharSequence>()
        class InjectionTest{
            val myString: String = get()
            val myCharSequence: CharSequence = get()
        }
        val injectionTest = InjectionTest()
        assertEquals("", injectionTest.myString)
        assertEquals("MY_CHAR_SEQUENCE", injectionTest.myCharSequence)
    }

}