infix fun <T> List<T>.drop_at(ix: Int): List<T> {
    return this.slice(0 until ix) + this.slice(ix + 1 until this.size)
}

infix fun <U, V> List<U>.cartesian_product(other: List<V>): List<Pair<U, V>> {
    return this.flatMap { x -> other.map { y -> Pair(x, y) } }
}

infix operator fun <U, V> List<U>.times(other: List<V>): List<Pair<U, V>> {
    return this cartesian_product other
}

/**
 * An init-less nullable fold with the following semantics:
 * - If the list is empty, return null
 * - If the list has one element, return that element
 * - Otherwise, fold the list with the given operator, and return the result. If a null is
 *   encountered or returned at any point, return null.
 */
fun <T> List<T?>.nullable_fold(op: (T, T) -> T?): T? {
    if (this.isEmpty()) {
        return null
    } else if (this.size == 1) {
        return this[0]
    } else {
        var acc = this[0]
        for (i in 1 until this.size - 1) {
            val next = this[i]
            if (acc == null || next == null) {
                return null
            }
            acc = op(acc, next)
        }
        return acc
    }
}
