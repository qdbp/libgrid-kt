package mdspan

fun Iterable<Int>.prod(): Int {
    return this.reduce { acc, i -> acc * i }
}

fun <T> List<T>.mdspan(vararg shape: Int): MDSpan<T> {
    return MDSpan(this, shape.toList())
}

fun ndindex(vararg shape: Int): List<List<Int>> {
    require(shape.all { it > 0 })
    val indices = MutableList(shape.size) { 0 }
    val max_indices = shape.map { it - 1 }
    val result = mutableListOf<List<Int>>()
    for (i in 0 until shape.toList().prod()) {
        result.add(indices.toList())
        for (j in indices.indices.reversed()) {
            indices[j] += 1
            if (indices[j] <= max_indices[j]) {
                break
            }
            indices[j] = 0
        }
    }
    return result
}

sealed class SliceLike

object ALL : SliceLike()

class SL(val start: Int, val end: Int) : SliceLike()

class IX(val index: Int) : SliceLike()

/**
 * numpy ndarray like view over a flat list.
 *
 * provides convenient methods for slicing and indexing.
 *
 * inherits the Iterable interface from List.
 */
open class MDSpan<out T>(open val data: List<T>, val shape: List<Int>) : List<T> by data {
    init {
        require(data.size == shape.prod())
    }

    operator fun get(vararg indices: Int): T {
        return data[get_offset(*indices)]
    }

    fun ravel(): List<T> {
        return data
    }

    operator fun get(vararg indices: SliceLike): MDSpan<T> {
        val new_shape = mutableListOf<Int>()
        for ((i, s) in shape.withIndex()) {
            when (val index = indices[i]) {
                is ALL -> new_shape.add(s)
                is SL -> {
                    require(index.start >= 0)
                    require(index.end <= s)
                    new_shape.add(index.end - index.start)
                }
                is IX -> new_shape.add(1)
            }
        }

        val new_data = mutableListOf<T>()

        for (new_ndix in ndindex(*new_shape.toIntArray())) {
            val shifted_index: List<Int> =
                new_ndix.zip(indices).map {
                    when (val index = it.second) {
                        is ALL -> it.first
                        is SL -> it.first + index.start
                        is IX -> index.index
                    }
                }
            new_data.add(this.get(*shifted_index.toIntArray()))
        }

        return MDSpan(new_data, new_shape)
    }

    protected fun get_offset(vararg indices: Int): Int {
        require(indices.size == shape.size)
        require(indices.all { it >= 0 })
        var offset = 0
        for ((i, s) in indices.zip(shape)) {
            require(i in 0 until s)
            offset *= s
            offset += i
        }
        return offset
    }

}

class MutableMDSpan<T>(override val data: MutableList<T>, shape: List<Int>) :
    MDSpan<T>(data, shape) {

    operator fun set(vararg indices: Int, value: T) {
        data[get_offset(*indices)] = value
    }
}
