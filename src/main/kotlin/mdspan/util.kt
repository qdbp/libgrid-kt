package mdspan

fun Iterable<Int>.prod(): Int {
    return this.reduce { acc, i -> acc * i }
}

fun <T> List<T>.mdspan(vararg shape: Int): NDSpanImpl<T> {
    return NDSpanImpl(this, shape.toList())
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

fun ndindex(shape: List<Int>): List<List<Int>> {
    return ndindex(*shape.toIntArray())
}
