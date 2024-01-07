package grid_model.geom

/**
 * A vector is a glorified List<Int> with some nice geometric operations.
 *
 * We delegate to List<Int> for low level stuff. This also lets us gracefully degrade into a
 * still-useful object in dimension-erased logic.
 *
 * We implement a lattice over vectors with the following relation: a <= b <=> ∀i: a(i) <= b(i)
 *
 * Then the GLB is the element-wise min, and the LUB is the element-wise max.
 */
// dummy type parameter keeps us from mixing up vectors of different dimensions; not that
// inside the body of the class we use the *outer* D, not the  one.
// TODO I'm really not convinced that Vec: Lattice makes sense... we can have multiple
//  lattice impls for vec. It should really be:
//      object VecLattice: Lattice<Vec> { Vec.join(Vec), ... }
//  and then we would bring it into scope as `with(MyVecLattice)` -- injection style
class Vec<D : Dim<D>>(private val coords: List<Int>, val dim: D) : List<Int> by coords {

    operator fun plus(other: Vec<D>): Vec<D> {
        return dim.vec(coords.zip(other.coords).map { (a, b) -> a + b })
    }

    operator fun minus(other: Vec<D>): Vec<D> {
        return dim.vec(coords.zip(other.coords).map { (a, b) -> a - b })
    }

    operator fun unaryMinus(): Vec<D> {
        return dim.vec(coords.map { -it })
    }

    override fun toString(): String = "$coords".replace(" ", "")

    fun to_origin_bb(): BBox<D> = BBox(dim.zeros(), this)

    fun to_bb(other: Vec<D>): BBox<D> = BBox(this, other)

    override fun equals(other: Any?): Boolean {
        if (other !is Vec<*>) return false
        return coords == other.coords // dim check is implicit here
    }

    override fun hashCode(): Int = coords.hashCode()

    /** Given a vector (x, y, z, ...), returns all combinations of (+-x, +-y, +-z, ...) */
    fun symmetrize_quadrants(): Set<Vec<D>> {
        val out = mutableSetOf<Vec<D>>()
        for (i in 0 ..< (1 shl dim.ndim)) {
            val signs = List(dim.ndim) { if ((i shr it) and 1 == 0) 1 else -1 }
            out.add(dim.vec(coords.zip(signs).map { (a, b) -> a * b }))
        }
        return out
    }
}
