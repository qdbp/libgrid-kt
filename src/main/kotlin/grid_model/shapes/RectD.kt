package grid_model.shapes

import grid_model.Shape
import grid_model.dimension.BBox
import grid_model.dimension.Dim
import grid_model.dimension.Vec
import grid_model.dimension.Vec.Companion.zvec

class RectD<D : Dim<D>>(val lower: Vec<D>, val upper: Vec<D>, dim: D) : Shape<D>(dim) {
    constructor(upper: Vec<D>, dim: D) : this(dim.zvec(), upper, dim)

    constructor(bb: BBox<D>) : this(bb.lower, bb.upper, bb.dim)

    // synergy
    override fun compute_points(): Set<Vec<D>> = BBox(lower, upper).points().toSet()
}
