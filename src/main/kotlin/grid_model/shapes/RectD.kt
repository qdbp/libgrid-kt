package grid_model.shapes

import grid_model.Shape
import grid_model.dimension.*
import grid_model.dimension.Vec.Companion.ones
import grid_model.dimension.Vec.Companion.vec
import grid_model.dimension.Vec.Companion.zvec
import ivory.order.PartialOrder.Companion.pleq

open class RectD<D : Dim<D>>(val lower: Vec<D>, val upper: Vec<D>, dim: D) : Shape<D>(dim) {
    constructor(upper: Vec<D>, dim: D) : this(dim.zvec(), upper, dim)

    constructor(bb: BBox<D>) : this(bb.lower, bb.upper, bb.dim)

    init {
        dim.vlat.run { require(lower pleq upper) { "lower $lower !<= upper $upper" } }
    }

    // synergy
    override fun compute_points(): Set<Vec<D>> = BBox(lower, upper).points().toSet()

    companion object {

        private inline fun <reified D : Dim<D>> rect(vararg coords: Int): RectD<D> {
            val d = D::class.fix()
            return RectD(d.vec(*coords) - d.ones(), d)
        }

        fun rect(w: Int, h: Int): RectD<D2> = rect<D2>(w, h)

        fun rect(w: Int, h: Int, d: Int): RectD<D3> = rect<D3>(w, h, d)
    }
}
