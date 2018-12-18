package Week4

import Week4.Mocker.any
import Week4.Mocker.eq
import Week4.Mocker.isNull
import Week4.Mocker.mock
import Week4.Mocker.setBody
import Week4.Mocker.setReturnValue
import org.junit.Test
import java.util.concurrent.ForkJoinPool
import kotlin.test.assertEquals

interface Example1 {
    fun getInt(): Int
}

interface Example2 {
    fun a(i: Int, str: String)
}

interface Example3 {
    fun nullable(i: Double, str: String?)
}

class MockTests{
    @Test
    fun `Basic test`() {
        val a = mock<Example1>()
        setReturnValue({ a.getInt() }, 2)
        assertEquals(2, a.getInt())

        var i = 1
        val b = mock<Example1>()
        setBody({ b.getInt() }, { i++ })
        assertEquals(1, b.getInt())
        assertEquals(2, b.getInt())
        assertEquals(3, b.getInt())
    }

    @Test
    fun `Arguments test`() {
        val b = mock<Example2>()
        setBody({ b.a(any(), any()) }, { (a, str) -> println("$a, $str") })
        setBody({ b.a(eq(1), any()) }, { println("This is one!") })
        setBody({ b.a(eq(0), any())}, {println("This is the zero!")})
        b.a(10, "AAA") // Prints: 10, AAA
        b.a(5, "AAA") // Prints: 5, AAA
        b.a(1, "AAA") // Prints: This is one!
        b.a(0, "AAA") // Prints: This is zero!
    }

    @Test
    fun `Nullable test`() {
        val b = mock<Example3>()
        setBody({ b.nullable(any(), isNull()) }, { (a, str) -> println("This is $a and null!") })
        setBody({ b.nullable(eq(1.5), isNull()) }, { println("This is 1 and half and null!") })
        setBody({ b.nullable(eq(0.0), isNull())}, {println("This is zero and null!")})
        setBody({ b.nullable(any(), any())}, {println("This any stuff really")})
        b.nullable(10.0, null) // Prints: This is 1.0 and null!
        b.nullable(5.0, null) // Prints: This is 5.0 and null!
        b.nullable(1.5, null) // Prints: This is 1 and half and null!
        b.nullable(0.0, null) // Prints: This is zero and null!
        b.nullable(5.0, "STUFF") // Prints: This is any stuff really...
    }

    @Test
    fun `Multithreaded test`() {
        (1..1_000_000).map {
            ForkJoinPool.commonPool().submit{
                val a = mock<Example1>()
                setReturnValue({ a.getInt() }, 2)
                assertEquals(2, a.getInt())

                var i = 1
                val b = mock<Example1>()
                setBody({ b.getInt() }, { i++ })
                assertEquals(1, b.getInt())
                assertEquals(2, b.getInt())
                assertEquals(3, b.getInt())
            }
        }.forEach {
            it.join()
        }
    }
}

