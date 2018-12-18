package Week4

import Week4.consts.EQUALS_METHOD
import Week4.consts.HASH_CODE_METHOD
import Week4.consts.PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES
import Week4.consts.TO_STRING_METHOD
import Week4.core.MockEvaluator
import Week4.core.MockType
import Week4.exception.ProxyControlException
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object Mocker {
    val anyDefaultCache = ConcurrentHashMap<Any, Any>()
    val mockMap = ThreadLocal.withInitial { WeakHashMap<Any, MockEvaluator>() }
    val currentMockArgs = ThreadLocal.withInitial { mutableListOf<MockType>() }
    val currentMockMethod = ThreadLocal<(Array<out Any?>) -> Any?>()

    inline fun <reified T> mock(): T {
        val mock = Proxy.newProxyInstance(
                T::class.java.classLoader,
                arrayOf(T::class.java)
        ) { proxy: Any, method: Method, args: Array<Any>? ->
            when (method) {
                // Handle methods inherited from Object
                HASH_CODE_METHOD -> System.identityHashCode(proxy)
                EQUALS_METHOD -> proxy === args?.get(0) ?: false
                TO_STRING_METHOD -> proxy.javaClass.name + '@' + Integer.toHexString(proxy.hashCode())
                else -> {
                    val mock = mockMap.get()[proxy]
                        ?: throw IllegalArgumentException("This object has not been registered as a mock")
                    val currentValue = currentMockMethod.get()
                    if (currentValue == null) mock.eval(method, args ?: emptyArray())
                    else mock.add(method, currentMockArgs.get(), currentValue)
                }
            }
        } as T
        mockMap.get()[mock] = MockEvaluator()
        return mock
    }

    fun <T> setReturnValue(method: () -> T, returnValue: T) = setBody(method, { returnValue })

    fun <T> setBody(method: () -> T, body: (Array<out Any?>) -> T) {
        currentMockMethod.set(body)
        try {
            method.invoke()
        } catch (control: ProxyControlException) {
            // Types might not match on return when we are invoking the mocked method here (e.g. nullable/non-nullable) so ClassCastExceptions could happen.
            // In order to handle the mocking logic, we throw an Exception we can control.
        } finally {
            currentMockMethod.remove()
            currentMockArgs.remove()
        }
    }

    inline fun <reified T : Any> any(): T {
        currentMockArgs.get().add(MockType.AnyType)
        return anyDefaultCache.computeIfAbsent(T::class.java) {
            PRIMITIVE_OR_WRAPPER_DEFAULT_VALUES[T::class.java] ?: T::class.java.newInstance()
        } as T
    }

    fun <T> eq(instance: T): T {
        currentMockArgs.get().add(MockType.EqualType(instance))
        return instance
    }

    fun <T> isNull(): T? {
        currentMockArgs.get().add(MockType.NullType)
        return null
    }
}
