/**
 * You can take the Python programmer out of Python, but you can't take the Python out of the Python
 * programmer
 */
fun range(stop: Int): Iterable<Int> = (0 ..< stop)

/**
 * You can take the Python programmer out of Python, but you can't take the Python out of the Python
 * programmer
 */
fun range(start: Int, stop: Int, step: Int = 1): Iterable<Int> = (start ..< stop).step(step)
