package grid_model.shapes

import grid_model.Shape
import grid_model.dimension.Dim
import grid_model.dimension.Vec
import grid_model.dimension.Vec.Companion.vec
import grid_model.dimension.fix

class FreeShape<D : Dim<D>>(val raw_points: List<List<Int>>, dim: D) : Shape<D>(dim) {
    override fun compute_points(): Set<Vec<D>> = raw_points.map { dim.vec(it) }.toSet()

    companion object {
        inline fun <reified D : Dim<D>> free(points: List<List<Int>>): FreeShape<D> =
            FreeShape(points, D::class.fix())
    }
}
