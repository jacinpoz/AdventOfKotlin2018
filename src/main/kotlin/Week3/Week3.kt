package Week3

import java.util.*

interface SortedMutableList<T> : Iterable<T> {
    val size: Int
    fun add(element: T)
    fun remove(element: T)
    operator fun get(index: Int): T
    operator fun contains(element: T): Boolean
}

class SortedArrayList<T>(vararg elements: T,
                         private val comp: Comparator<T>) : SortedMutableList<T> {
    private val list = mutableListOf<T>()

    init {
        elements.forEach { add(it) }
    }

    private fun findIndex(element: T): Int = list.binarySearch(element, comp)

    private tailrec fun findEquals(index: Int, element: T, step: Int): Boolean = when {
        index !in 0..(size - 1) -> false
        comp.compare(element, list[index]) != 0 -> false
        list[index] == element -> true
        else -> findEquals(index + step, element, step)
    }

    override val size: Int
        get() = list.size

    override fun add(element: T) = findIndex(element).let { index -> list.add(if (index < 0) -(index + 1) else index, element) }

    override fun remove(element: T) = findIndex(element).let { index -> if (index >= 0) list.removeAt(index) }

    override fun get(index: Int): T = list[index]

    override fun contains(element: T) = findIndex(element).let { index ->
        index >= 0 && element == list[index] || (findEquals(index + 1, element, 1) || findEquals(index - 1, element, -1))
    }

    override fun iterator(): Iterator<T> = list.iterator()
}

fun <T : Comparable<T>> sortedMutableListOf(vararg elements: T): SortedMutableList<T> = SortedArrayList(*elements, comp = compareBy { it })

fun <T> sortedMutableListOf(comparator: Comparator<T>, vararg elements: T): SortedMutableList<T> = SortedArrayList(elements = *elements, comp = comparator)