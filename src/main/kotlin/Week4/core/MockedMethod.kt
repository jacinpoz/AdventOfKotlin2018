package Week4.core

// Container with arguments and function body for each mocked  method.
// It also provides score calculations to identify what is the most suitable mocked method for each method call
class MockedMethod(private val arguments: List<MockType>,
                   val body: (Array<out Any?>) -> Any?) {
    fun getScore(args: Array<Any>): Int = when {
        args.size != arguments.size -> -1
        else -> arguments.foldIndexed(0) { index, acc, mockType ->
            if (acc < 0) {
                acc
            } else {
                val score = mockType.score(args[index])
                if (score >= 0) acc + score else score
            }
        }
    }
}