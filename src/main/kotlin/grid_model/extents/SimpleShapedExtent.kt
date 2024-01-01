package grid_model.extents

import boolean_algebra.BooleanExpr
import boolean_algebra.BooleanExpr.Companion.and
import boolean_algebra.BooleanExpr.Companion.pred
import grid_model.Extent
import grid_model.dimension.Dim
import grid_model.dimension.Vec
import grid_model.planes.Plane
import grid_model.predicate.SPGP
import grid_model.predicate.SinglePointCondition

/**
 * A simple shaped extent is an extent whose demands are a simple `And` over a collection of point
 * disjoint point predicates.
 */
abstract class SimpleShapedExtent<D : Dim<D>> : Extent<D>() {

    final override fun render_demands_within(plane: Plane): BooleanExpr<SPGP<D>> {
        return and(
            get_point_demands(plane).map { (coords, predicate) -> pred(predicate at coords) }
        )
    }

    abstract fun get_point_demands(plane: Plane): Map<Vec<D>, SinglePointCondition>
}
