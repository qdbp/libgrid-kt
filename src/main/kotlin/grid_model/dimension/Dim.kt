package grid_model.dimension

import grid_model.dimension.Vec.Companion.ones
import grid_model.dimension.Vec.Companion.vec
import ivory.order.BoundedLattice
import ivory.order.Lattice
import ivory.order.PartialOrder.Companion.pgeq
import ivory.order.PartialOrder.Companion.pleq
import ivory.order.Rel
import mdspan.ndindex
import requiring
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass

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
    @Suppress("UNCHECKED_CAST", "LeakingThis") val vlat = VecLattice(this as D)
    val blat = BBLattice()
}

inline fun <reified D : Dim<D>> KClass<D>.fix(): D {
    return when (this) {
        D1::class -> D1
        D2::class -> D2
        D3::class -> D3
        D4::class -> D4
        else -> error("unsupported dimension $this")
    }
        as D
}

data object D1 : Dim<D1>(1)

data object D2 : Dim<D2>(2)

data object D3 : Dim<D3>(3)

data object D4 : Dim<D4>(4)

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
            return Vec(coords requiring { it.size == ndim }, dim = this)
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

    override fun toString(): String = "$coords".replace(" ", "")

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

    override infix fun Vec<D>.cmp(other: Vec<D>): Rel? {
        val zipped = this.zip(other)
        return when {
            zipped.all { (a, b) -> a <= b } -> Rel.LEQ
            zipped.all { (a, b) -> a >= b } -> Rel.GEQ
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
class BBox<D : Dim<D>>(a: Vec<D>, b: Vec<D>) : Iterable<Vec<D>>, Collection<Vec<D>> {

    val dim = a.dim
    val lower = a.dim.vlat.run { a meet b }
    val upper = a.dim.vlat.run { a join b }

    operator fun component1(): Vec<D> = lower

    operator fun component2(): Vec<D> = upper

    // need to add ones since bounding boxes are inclusive, so upper == lower actually implies
    // a shape of ones
    val shape: Vec<D> by lazy { upper - lower + dim.ones() }

    fun points(): Iterable<Vec<D>> {
        return ndindex(shape).map { lower + lower.dim.vec(it) }
    }

    override fun iterator(): Iterator<Vec<D>> = points().iterator()

    override val size: Int
        get() = points().count()

    override fun isEmpty(): Boolean = false

    override fun containsAll(elements: Collection<Vec<D>>): Boolean = elements.all { it in this }

    override operator fun contains(element: Vec<D>): Boolean =
        with(element.dim.vlat) { element pgeq lower && element pleq upper }
}

/** null is empty b-box */
class BBLattice<D : Dim<D>> : Lattice<BBox<D>?> {

    override infix fun (BBox<D>?).cmp(other: BBox<D>?): Rel? {
        if (this == null) return Rel.LEQ
        if (other == null) return Rel.GEQ
        return with(dim.vlat) {
            when {
                lower pleq other.lower && upper pgeq other.upper -> Rel.GEQ
                lower pgeq other.lower && upper pleq other.upper -> Rel.LEQ
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
