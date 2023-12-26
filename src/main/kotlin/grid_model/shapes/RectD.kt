package grid_model.shapes

import grid_model.Shape
import grid_model.dimension.BBox
import grid_model.dimension.D2
import grid_model.dimension.Dim
import grid_model.dimension.Vec
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
}

class Rect2(lower: Vec<D2>, upper: Vec<D2>) : RectD<D2>(lower, upper, D2) {

    constructor(w: Int, h: Int) : this(D2.vec(0, 0), D2.vec(w - 1, h - 1))
}
