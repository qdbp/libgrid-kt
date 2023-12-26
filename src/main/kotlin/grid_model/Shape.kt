package grid_model

import grid_model.dimension.BBox
import grid_model.dimension.Dim
import grid_model.dimension.Vec
import grid_model.shapes.RectD
import ivory.order.BoundedLattice.Companion.join
import ivory.order.BoundedLattice.Companion.meet

fun interface BaseShape<D : Dim<D>> {
    fun compute_points(): Set<Vec<D>>
}

/**
 * In grid land, a shape is just a collection of occupied relative coordinates points.
 *
 * While there is no intrinsic restriction on what these points are, there is an interpretation that
 * is assumed: an [Entity] with an [Extent] conforming to a [Shape] is interpreted as being at the
 * origin of the shape's coordinates. If these coordinates are e.g. shifted weirdly, the problem
 * specification will become very unintuitive.
 *
 * Generally, each shape should strive to contain the origin in its bounding box.
 *
 * Simple shapes defined in this module will adhere to the convention that the origin is the
 * bottom-"left" corner of the bounding box, where left is interpreted as toward zero.
 */
abstract class Shape<D : Dim<D>>(val dim: D) : BaseShape<D> {

    abstract override fun compute_points(): Set<Vec<D>>

    // shapes are immutable, so we can cache the points
    val points: Set<Vec<D>> by lazy { compute_points() }

    /** [inclusive, inclusive] bounding box */
    private fun bounding_box(): BBox<D>? {
        when (points.size) {
            0 -> return null
            else -> {
                with(dim.vlat) {
                    val lower = points.meet()
                    val upper = points.join()
                    return BBox(lower, upper)
                }
            }
        }
    }

    fun translated(shift: Vec<D>): Shape<D> {
        require(shift.size == dim.ndim)
        return object : Shape<D>(dim) {
            override fun compute_points(): Set<Vec<D>> =
                points.map { coords -> coords + shift }.toSet()
        }
    }

    // returns a shape that is the complement of this shape within its bounding box
    fun complement(): Shape<D> {
        return when (val bb = bounding_box()) {
            null ->
                object : Shape<D>(dim) {
                    override fun compute_points(): Set<Vec<D>> = setOf()
                }
            else -> {
                RectD(bb)
            }
        }
    }

    // because shapes are just sets of points, we can define addition and subtraction as
    // set operations to get easy shape composition
    operator fun plus(other: Shape<D>): Shape<D> =
        object : Shape<D>(dim) {
            override fun compute_points(): Set<Vec<D>> = this.points + other.points
        }

    operator fun minus(other: Shape<D>): Shape<D> =
        object : Shape<D>(dim) {
            override fun compute_points(): Set<Vec<D>> = this.points - other.points
        }

    fun intersect(other: Shape<D>): Shape<D> =
        object : Shape<D>(dim) {
            override fun compute_points(): Set<Vec<D>> = this.points intersect other.points
        }
}
