package grid_model.extents

import boolean_algebra.And
import boolean_algebra.BooleanAlgebra
import boolean_algebra.Pred
import grid_model.Extent
import grid_model.Plane
import grid_model.predicate.SPGP
import grid_model.predicate.SinglePointCondition

/**
 * A simple shaped extent is an extent whose demands are a simple `And` over a collection of point
 * disjoint point predicates.
 */
abstract class SimpleShapedExtent<P : Plane> : Extent<P>() {
    final override fun local_demands(): BooleanAlgebra<SPGP> {
        return And(get_point_demands().map { (coords, predicate) -> Pred(predicate at coords) })
    }

    abstract fun get_point_demands(): Map<List<Int>, SinglePointCondition>
}
