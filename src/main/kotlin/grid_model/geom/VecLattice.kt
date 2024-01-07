package grid_model.geom

import ivory.order.BoundedLattice
import ivory.order.Rel

class VecLattice<D : Dim<D>>(private val dim: D) : BoundedLattice<Vec<D>> {

    override val top: Vec<D> by lazy { dim.ones(Int.MAX_VALUE) }
    override val bottom: Vec<D> by lazy { dim.ones(Int.MIN_VALUE) }

    override infix fun Vec<D>.cmp(other: Vec<D>): Rel? {
        val zipped = this.zip(other)
        return when {
            zipped.all { (a, b) -> a <= b } -> Rel.LEQ
            zipped.all { (a, b) -> a >= b } -> Rel.GEQ
            else -> null
        }
    }

    override infix fun Vec<D>.join(other: Vec<D>): Vec<D> =
        dim.vec(this.zip(other).map { (a, b) -> kotlin.math.max(a, b) })

    override infix fun Vec<D>.meet(other: Vec<D>): Vec<D> =
        dim.vec(this.zip(other).map { (a, b) -> kotlin.math.min(a, b) })
}
