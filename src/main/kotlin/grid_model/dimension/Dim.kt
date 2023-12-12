package grid_model.dimension

import grid_model.dimension.Vec.Companion.ones
import grid_model.dimension.Vec.Companion.vec
import ivory.order.BoundedLattice
import ivory.order.Lattice
import ivory.order.Poset
import ivory.order.Poset.Companion.pgeq
import ivory.order.Poset.Companion.pleq
import kotlin.math.max
import kotlin.math.min
import mdspan.ndindex

interface BaseDim {
    val ndim: Int
}

/**
 * We're continuing the good pattern established with LPDomain here:
 *
 * create a "reified type class" with lots of useful fundamental math, and embed an instance of this
 * deep into the bone marrow of the other domain objects.
 */
sealed class Dim<D : Dim<D>>(final override val ndim: Int) : BaseDim {
    @Suppress("UNCHECKED_CAST") val vlat = VecLattice(this as D)
    val blat = BBLattice()
}

object D1 : Dim<D1>(1)

object D2 : Dim<D2>(2)

object D3 : Dim<D3>(3)

object D4 : Dim<D4>(4)

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
class Vec<D : Dim<D>> private constructor(private val coords: List<Int>, val dim: D) :
    List<Int> by coords, BaseDim by dim {

    companion object {
        fun <D : Dim<D>> D.vec(coords: List<Int>): Vec<D> {
            require(coords.size == ndim)
            return Vec(coords, dim = this)
        }

        fun <D : Dim<D>> D.vec(vararg coords: Int): Vec<D> = Vec(coords.toList(), dim = this)

        fun <D : Dim<D>> D.zvec(): Vec<D> = Vec(List(ndim) { 0 }, dim = this)

        fun <D : Dim<D>> D.ones(): Vec<D> = Vec(List(ndim) { 1 }, dim = this)

        fun <D : Dim<D>> D.ones(fill: Int) = Vec(List(ndim) { fill }, dim = this)
    }

    operator fun plus(other: Vec<D>): Vec<D> {
        return dim.vec(coords.zip(other.coords).map { (a, b) -> a + b })
    }

    operator fun minus(other: Vec<D>): Vec<D> {
        return dim.vec(coords.zip(other.coords).map { (a, b) -> a - b })
    }

    operator fun unaryMinus(): Vec<D> {
        return dim.vec(coords.map { -it })
    }

    override fun toString(): String = "Vec$coords".replace(" ", "")

    fun to_origin_bb(): BBox<D> = BBox(dim.zvec(), this)

    fun to_bb(other: Vec<D>): BBox<D> = BBox(this, other)

    override fun equals(other: Any?): Boolean {
        if (other !is Vec<*>) return false
        return coords == other.coords // dim check is implicit here
    }

    override fun hashCode(): Int = coords.hashCode()
}

class VecLattice<D : Dim<D>>(private val dim: D) : BoundedLattice<Vec<D>> {

    override val top: Vec<D> by lazy { dim.ones(Int.MAX_VALUE) }
    override val bottom: Vec<D> by lazy { dim.ones(Int.MIN_VALUE) }

    override infix fun Vec<D>.pcmp(other: Vec<D>): Poset.Rel? {
        val zipped = this.zip(other)
        return when {
            zipped.all { (a, b) -> a <= b } -> Poset.Rel.LEQ
            zipped.all { (a, b) -> a >= b } -> Poset.Rel.GEQ
            else -> null
        }
    }

    override infix fun Vec<D>.join(other: Vec<D>): Vec<D> =
        dim.vec(this.zip(other).map { (a, b) -> max(a, b) })

    override infix fun Vec<D>.meet(other: Vec<D>): Vec<D> =
        dim.vec(this.zip(other).map { (a, b) -> min(a, b) })
}

/**
 * Non-empty bounding boxes: axis-aligned hyper-rectangles.
 *
 * Because we restrict ourselves to non-empty BBs, we do not have a meet. TODO worth adding? need
 * some sort of sealed hierarchy? yuck
 */
class BBox<D : Dim<D>>(a: Vec<D>, b: Vec<D>) {

    val lower = with(a.dim.vlat) { a meet b }
    val upper = with(a.dim.vlat) { a join b }
    val dim = a.dim

    operator fun component1(): Vec<D> = lower

    operator fun component2(): Vec<D> = upper

    fun points(): Iterable<Vec<D>> {
        return ndindex(upper - lower).map { lower + lower.dim.vec(it) }
    }

    operator fun contains(other: Vec<D>): Boolean =
        with(other.dim.vlat) { other pgeq lower && other pleq upper }
}

/** null is empty b-box */
class BBLattice<D : Dim<D>> : Lattice<BBox<D>?> {

    override infix fun (BBox<D>?).pcmp(other: BBox<D>?): Poset.Rel? {
        if (this == null) return Poset.Rel.LEQ
        if (other == null) return Poset.Rel.GEQ
        return with(dim.vlat) {
            when {
                lower pleq other.lower && upper pgeq other.upper -> Poset.Rel.GEQ
                lower pgeq other.lower && upper pleq other.upper -> Poset.Rel.LEQ
                else -> null
            }
        }
    }

    override fun (BBox<D>?).meet(other: BBox<D>?): BBox<D>? {
        if (this == null) return null
        if (other == null) return null
        return with(other.dim.vlat) {
            if (
                (lower pgeq other.upper && lower != other.upper) ||
                    (upper pleq other.lower && upper != other.lower)
            ) {

                BBox(lower join other.lower, upper meet other.upper)
            } else {
                null
            }
        }
    }

    /** The join is the smallest BBox containing both. */
    override infix fun (BBox<D>?).join(other: BBox<D>?): BBox<D>? {
        if (this == null) return other
        if (other == null) return this
        return with(dim.vlat) { BBox(lower meet other.lower, upper join other.upper) }
    }
}
