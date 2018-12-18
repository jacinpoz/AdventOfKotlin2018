package Week4.core

import Week4.exception.ProxyControlException
import java.lang.reflect.Method

class MockEvaluator {
    private val mockedMethods: MutableMap<String, MutableList<MockedMethod>> = mutableMapOf()

    fun add(method: Method, args: List<MockType>, value: (Array<out Any?>) -> Any?): Any? {
        mockedMethods.computeIfAbsent(method.toString()) { mutableListOf() }.add(MockedMethod(args, value))
        throw ProxyControlException
    }

    fun eval(method: Method, args: Array<Any>): Any? {
        val methodList = mockedMethods[method.toString()] ?: throw IllegalArgumentException("This method is not mocked")
        val mockedMethod = methodList.map { it.getScore(args) to it }.maxBy { it.first }
        if (mockedMethod == null || mockedMethod.first < 0) throw IllegalAccessError("No valid mocked method found")
        return mockedMethod.second.body(args)
    }
}