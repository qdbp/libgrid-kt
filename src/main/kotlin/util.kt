infix fun <T> List<T>.drop_at(ix: Int): List<T> {
    return this.slice(0 ..< ix) + this.slice(ix + 1 ..< this.size)
}

infix fun <U, V> Iterable<U>.cartesian_product(other: Iterable<V>): List<Pair<U, V>> {
    return this.flatMap { x -> other.map { y -> Pair(x, y) } }
}

infix operator fun <U, V> Iterable<U>.times(other: Iterable<V>): List<Pair<U, V>> {
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
        for (i in 1 ..< this.size - 1) {
            val next = this[i]
            if (acc == null || next == null) {
                return null
            }
            acc = op(acc, next)
        }
        return acc
    }
}

// TODO spin these cutesies off to a separate "words of power" library or something
/** I love extension functions. */
infix fun <T> Any?.then(it: T): T = it

infix fun <T> T.after(that: Any?): T = this

infix fun <T> T.requiring(v: Boolean) = require(v) then this

infix fun <T> T.requiring(op: (T) -> Boolean) = require(op(this)) then this

infix fun <T> T.granted(v: Boolean): T? = if (v) this else null

infix fun <T> T.granted(op: (T) -> Boolean): T? = if (op(this)) this else null
