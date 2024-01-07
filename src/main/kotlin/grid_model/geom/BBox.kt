package grid_model.geom

import ivory.order.PartialOrder.Companion.pgeq
import ivory.order.PartialOrder.Companion.pleq
import mdspan.ndindex
import requiring

/**
 * Non-empty bounding boxes: axis-aligned hyper-rectangles.
 *
 * Because we restrict ourselves to non-empty BBs, we do not have a meet. TODO worth adding? need
 * some sort of sealed hierarchy? yuck
 */
class BBox<D : Dim<D>>(a: Vec<D>, b: Vec<D>) : Iterable<Vec<D>>, Collection<Vec<D>> {

    companion object {
        inline operator fun <reified D : Dim<D>> invoke(vararg size: Int): BBox<D> {
            val dim = D::class.fix() requiring { size.size == it.ndim }
            return BBox(dim.zeros(), dim.vec(size.toList()) - dim.ones())
        }
    }

    val dim = a.dim

    val lower = dim.vlat.run { a meet b }
    val upper = dim.vlat.run { a join b }

    operator fun component1(): Vec<D> = lower

    operator fun component2(): Vec<D> = upper

    // need to add ones since bounding boxes are inclusive, so upper == lower actually implies
    // a shape of ones
    val shape: Vec<D> by lazy { upper - lower + dim.ones() }

    fun points(): Iterable<Vec<D>> = ndindex(shape).map { lower + dim.vec(it) }

    override fun iterator(): Iterator<Vec<D>> = points().iterator()

    override val size: Int
        get() = points().count()

    override fun isEmpty(): Boolean = false

    override fun containsAll(elements: Collection<Vec<D>>): Boolean = elements.all { it in this }

    override operator fun contains(element: Vec<D>): Boolean =
        dim.vlat.run { element pgeq lower && element pleq upper }
}
