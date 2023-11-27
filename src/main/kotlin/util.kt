infix fun <T> List<T>.drop_at(ix: Int): List<T> {
    return this.slice(0 until ix) + this.slice(ix + 1 until this.size)
}

infix fun <U, V> List<U>.cartesian_product(other: List<V>): List<Pair<U, V>> {
    return this.flatMap { x -> other.map { y -> Pair(x, y) } }
}

infix operator fun <U, V> List<U>.times(other: List<V>): List<Pair<U, V>> {
    return this cartesian_product other
}
