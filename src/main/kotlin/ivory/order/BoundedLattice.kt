package ivory.order

interface BoundedLattice<T> : Lattice<T> {
    val top: T
    val bottom: T
}

context(BoundedLattice<T>)
fun <T> Iterable<T>.meet(): T = this.fold(top) { acc, t -> acc meet t }

context(BoundedLattice<T>)
fun <T> Iterable<T>.join(): T = this.fold(bottom) { acc, t -> acc join t }
