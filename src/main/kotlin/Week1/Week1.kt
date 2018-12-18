import it.unimi.dsi.fastutil.ints.Int2DoubleArrayMap
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap
import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap
import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

private const val WALL = 'B'

typealias Heuristic = (ax: Int, ay: Int, bx: Int, by: Int) -> Double

private val simpleHeuristic: Heuristic = { ax: Int, ay: Int, bx: Int, by: Int -> (abs(ax - bx) + (ay - by)).toDouble() }
private val standardHeuristic: Heuristic = { ax: Int, ay: Int, bx: Int, by: Int -> sqrt((ax - bx).toDouble().pow(2) + (ay - by).toDouble().pow(2)) }

class MapStructure(private val charMap: CharArray,
                   private val rowLength: Int,
                   private val rowNumber: Int,
                   private val startIndex: Int,
                   val endIndex: Int,
                   private val heuristic: Heuristic) {

    private val goalx = endIndex % rowLength
    private val goaly = endIndex / rowLength
    // Neighbours cache to avoid recomputing them again
    private val neighboursCache: Int2ReferenceArrayMap<IntArray> = Int2ReferenceArrayMap()

    fun cost(a: Int, b: Int): Double {
        val subtract = abs(a - b)
        return if (subtract == 1 || subtract == rowLength) 1.0 else 1.5
    }

    private fun MutableList<Int>.addNeighbour(index: Int) = if (charMap[index] != WALL) this.add(index) else false

    fun neighbours(point: Point): IntArray = neighboursCache.computeIfAbsent(point.index) {
        // 9 possible neighbours: Top left, Top, Top right, Left, Right, Bottom left, Bottom, Bottom right
        val neighboursList = mutableListOf<Int>()
        val leftNeighbourValid = point.x - 1 >= 0
        // Extra -1 because we have '\n' characters
        val rightNeighbourValid = point.x + 1 < (rowLength - 1)
        val topNeighbourValid = point.y - 1 >= 0
        val bottomNeighbourValid = point.y + 1 < rowNumber
        if (leftNeighbourValid) {
            // Left neighbours
            neighboursList.addNeighbour(point.index - 1)
            if (topNeighbourValid) neighboursList.addNeighbour(point.index - rowLength - 1)
            if (bottomNeighbourValid) neighboursList.addNeighbour(point.index + rowLength - 1)
        }
        if (topNeighbourValid) neighboursList.addNeighbour(point.index - rowLength)
        if (rightNeighbourValid) {
            neighboursList.addNeighbour(point.index + 1)
            if (topNeighbourValid) neighboursList.addNeighbour(point.index - rowLength + 1)
            if (bottomNeighbourValid) neighboursList.addNeighbour(point.index + rowLength + 1)
        }
        if (bottomNeighbourValid) neighboursList.addNeighbour(point.index + rowLength)
        neighboursList.toIntArray()
    }

    fun createPoint(index: Int, cost: Double): Point {
        val x = index % rowLength
        val y = index / rowLength
        return Point(index, x, y, cost + heuristic(x, y, goalx, goaly))
    }

    fun startPoint() = Point(startIndex, startIndex % rowLength, startIndex / rowLength, 0.0)
}

class Point(val index: Int, val x: Int, val y: Int, val priority: Double)

fun createMapAttributes(charMap: CharArray, heuristic: Heuristic): MapStructure {
    var rowLength = 0
    var rowNumber = 0
    var start = 0
    var end = 0
    charMap.forEachIndexed { index, c ->
        when (c) {
            '\n' -> {
                rowNumber++
                if (rowLength == 0) rowLength = index + 1
            }
            'S' -> start = index
            'X' -> end = index
        }
    }
    return MapStructure(charMap, rowLength, rowNumber + 1, start, end, heuristic)
}


fun addPath(mapString: String): String {
    // Use single char array to improve memory locality and performance
    val charMap = mapString.toCharArray()
    // Choose heuristic here
    val mapPoints = createMapAttributes(charMap, standardHeuristic)
    val start = mapPoints.startPoint()
    // Use fastutil primitive maps for increased performance
    val costSoFar = Int2DoubleArrayMap()
    val cameFrom = Int2IntArrayMap()
    //Pre-size queue to optimistically avoid enlarging it later
    val priorityQueue = PriorityQueue<Point>(mapString.length / 3, compareBy<Point> { it.priority })
    priorityQueue.add(start)
    var currentPoint = priorityQueue.poll()
    while (currentPoint != null) {
        if (currentPoint.index == mapPoints.endIndex) break
        for (nextPoint in mapPoints.neighbours(currentPoint)) {
            val newCost = costSoFar[currentPoint.index] + mapPoints.cost(currentPoint.index, nextPoint)
            val costSoFarForNext = costSoFar[nextPoint]
            if (costSoFarForNext == 0.0 || newCost < costSoFarForNext) {
                costSoFar[nextPoint] = newCost
                val point = mapPoints.createPoint(nextPoint, newCost)
                priorityQueue.add(point)
                cameFrom[nextPoint] = currentPoint.index
            }
        }
        currentPoint = priorityQueue.poll()
    }

    var backtrackPoints: Int? = currentPoint.index
    while (backtrackPoints != null && backtrackPoints != start.index) {
        charMap[backtrackPoints] = '*'
        backtrackPoints = cameFrom[backtrackPoints]
    }
    charMap[start.index] = '*'
    return charMap.joinToString(separator = "")
}


