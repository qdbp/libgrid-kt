package grid_model.extents

import boolean_algebra.And
import boolean_algebra.BooleanAlgebra
import boolean_algebra.Pred
import grid_model.dimension.Dim
import grid_model.dimension.Vec
import grid_model.predicate.SPGP
import grid_model.predicate.SinglePointCondition

/**
 * A simple shaped extent is an extent whose demands are a simple `And` over a collection of point
 * disjoint point predicates.
 */
abstract class SimpleShapedExtent<D : Dim<D>> : Extent<D>() {

    final override fun local_demands(): BooleanAlgebra<SPGP<D>> {
        return And(get_point_demands().map { (coords, predicate) -> Pred(predicate at coords) })
    }

    abstract fun get_point_demands(): Map<Vec<D>, SinglePointCondition>
}
