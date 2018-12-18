package Week2

import kotlin.LazyThreadSafetyMode.SYNCHRONIZED

// Never use Week2.getCreatorsRegistry directly!!! This is an internal API!
val creatorsRegistry: MutableMap<String, () -> Any?> = mutableMapOf()

fun createKey(moduleName: String, qualifiedName: String) = if (moduleName.isNotEmpty()) "$moduleName:$qualifiedName" else qualifiedName

fun className(name: String?): String = name.let { it ->
    require(it is String) { "Class qualified name for target type is missing" }
    it
}

inline fun <reified T> (() -> T).bind(moduleName: String = "") {
    creatorsRegistry[createKey(moduleName, className(T::class.java.canonicalName))] = this
}

inline fun <reified T> T.bind(moduleName: String = "") = { this }.bind(moduleName)

// Use "newInstance" when we can't find Week4.getA constructor as Week4.getA default value.
inline fun <reified T> get(moduleName: String = ""): T {
    return creatorsRegistry[createKey(moduleName, className(T::class.java.canonicalName))]?.invoke() as? T ?: T::class.java.newInstance()
}

inline fun <reified T> inject(moduleName: String = "", mode: LazyThreadSafetyMode = SYNCHRONIZED): Lazy<T> = lazy(mode) { get<T>(moduleName) }

class Module(val moduleName: String) {
    inline fun <reified T> T.bind() = { this }.bind(moduleName)
    inline fun <reified T> get() = get<T>(moduleName)
    inline fun <reified T> inject() = inject<T>(moduleName)
}

fun module(moduleName: String, init: Module.() -> Unit): Module = Module(moduleName).apply(init)