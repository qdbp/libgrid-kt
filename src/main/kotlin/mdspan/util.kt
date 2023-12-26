package mdspan

fun Iterable<Int>.prod(): Int {
    return when (this.count()) {
        0 -> 1
        else -> this.reduce { acc, i -> acc * i }
    }
}

fun <T> List<T>.mdspan(vararg shape: Int): NDSpanImpl<T> {
    return NDSpanImpl(this, shape.toList())
}

fun ndindex(vararg shape: Int): List<List<Int>> {
    require(shape.all { it > 0 }) { "shape must be all positive" }
    val indices = MutableList(shape.size) { 0 }
    val max_indices = shape.map { it - 1 }
    val next_ndix = mutableListOf<List<Int>>()
    for (i in 0 ..< shape.toList().prod()) {
        next_ndix.add(indices.toList())
        for (j in indices.indices.reversed()) {
            indices[j] += 1
            if (indices[j] <= max_indices[j]) {
                break
            }
            indices[j] = 0
        }
    }
    return next_ndix
}

fun ndindex(shape: List<Int>): List<List<Int>> {
    return ndindex(*shape.toIntArray())
}
