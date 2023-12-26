package ivory.order

import ivory.algebra.Monoid

interface BoundedLattice<T> : Lattice<T> {
    val top: T
    val bottom: T

    companion object {
        context(BoundedLattice<T>)
        fun <T> Iterable<T>.meet(): T = this.fold(top) { acc, t -> acc meet t }

        context(BoundedLattice<T>)
        fun <T> Iterable<T>.join(): T = this.fold(bottom) { acc, t -> acc join t }
    }
}

val <T> BoundedLattice<T>.join_monoid: Monoid<T>
    get() =
        object : Monoid<T> {
            override val id: T = bottom

            override infix fun T.op(other: T): T = this join other
        }

val <T> BoundedLattice<T>.meet_monoid: Monoid<T>
    get() =
        object : Monoid<T> {
            override val id: T = top

            override infix fun T.op(other: T): T = this meet other
        }
