package grid_model.geom

import ivory.algebra.Monoid
import ivory.order.BoundedLattice.Companion.join
import ivory.order.BoundedLattice.Companion.meet

/**
 * In grid land, a shape is just a collection of occupied points, relative to an unspecified origin.
 *
 * While there is no intrinsic restriction on what these points are, there is an interpretation that
 * is assumed: an [Entity] with a [Demand] conforming to a [Shape] is interpreted as being at the
 * origin of the shape's coordinates. If these coordinates are e.g. shifted weirdly, the problem
 * specification will become very unintuitive.
 *
 * Generally, each shape should strive to contain the origin in its bounding box.
 *
 * Simple shapes defined in this module will adhere to the convention that the origin is the
 * bottom-"left" corner of the bounding box, where left is interpreted as toward zero.
 */
class Shape<D : Dim<D>>(val points: Set<Vec<D>>, private val dim: D) {

    constructor(points: Collection<Vec<D>>, dim: D) : this(points.toSet(), dim)

    constructor(points: Collection<Vec<D>>) : this(points.toSet(), points.first().dim)

    /**
     * Big ol' laundry list of useful pre-defined shapes. Generally you want to use one of these
     * constructors.
     */
    companion object {
        fun <D : Dim<D>> rect(lower: Vec<D>, upper: Vec<D>): Shape<D> {
            return Shape(BBox(lower, upper).points().toSet(), lower.dim)
        }

        fun <D : Dim<D>> rect(bb: BBox<D>): Shape<D> = rect(bb.lower, bb.upper)

        fun <D : Dim<D>> rect(upper: Vec<D>): Shape<D> = rect(upper.dim.zeros(), upper)

        fun rect(w: Int, h: Int): Shape<D2> = rect(D2.vec(w, h) - D2.ones())

        fun rect(w: Int, h: Int, d: Int): Shape<D3> = rect(D3.vec(w, h, d) - D3.ones())
    }

    val monoid: Monoid<Shape<D>> by lazy { ShapeMonoid(dim) }

    private fun new(points: Set<Vec<D>>): Shape<D> = Shape(points, dim)

    /** [inclusive, inclusive] bounding box */
    private fun bounding_box(): BBox<D>? {
        when (points.size) {
            0 -> return null
            else -> {
                dim.vlat.run {
                    val lower = points.meet()
                    val upper = points.join()
                    return BBox(lower, upper)
                }
            }
        }
    }

    fun shifted(shift: Vec<D>): Shape<D> = new(points.map { coords -> coords + shift }.toSet())

    fun shifted(vararg shift: Int): Shape<D> = shifted(dim.vec(*shift))

    // returns a shape that is the complement of this shape within its bounding box
    fun complement(): Shape<D> =
        when (val bb = bounding_box()) {
            null -> empty(dim)
            else -> rect(bb) - this
        }

    fun truncate(to_bb: BBox<D>): Shape<D> = new(points.filter { to_bb.contains(it) }.toSet())

    // because shapes are just sets of points, we can define addition and subtraction as
    // set operations to get easy shape composition
    operator fun plus(other: Shape<D>): Shape<D> = new(points + other.points)

    operator fun minus(other: Shape<D>): Shape<D> = new(points - other.points)

    fun intersect(other: Shape<D>): Shape<D> = new(points intersect other.points)
}
