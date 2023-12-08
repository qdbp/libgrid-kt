/**
 * You can take the Python programmer out of Python, but you can't take the Python out of the Python
 * programmer
 */
fun range(stop: Int): Iterable<Int> {
    return Iterable { (0 until stop).iterator() }
}

/**
 * You can take the Python programmer out of Python, but you can't take the Python out of the Python
 * programmer
 */
fun range(start: Int, stop: Int, step: Int = 1): Iterable<Int> {
    return Iterable { (0 until stop).step(step).iterator() }
}
