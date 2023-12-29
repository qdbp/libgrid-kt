package grid_model.adapters.lp.pc

import grid_model.adapters.lp.GridLPChart
import grid_model.dimension.Dim
import grid_model.predicate.HasTile
import grid_model.predicate.IsEntity
import grid_model.predicate.NoneForPlane
import grid_model.predicate.SPGP
import kulp.LPAffExpr
import kulp.NodeCtx
import kulp.lp_sum
import kulp.transforms.ril.RIL

object PointPredicateRILCompiler : GridPredicateRILCompiler<SPGP<*>> {
    /**
     * Converts a predicate of the given type P to a RIL-expression that witnesses its truth.
     *
     * The ril expression should be an integer affine expr s.t. expr >= 1 <=> P is satisfied.
     */
    context(NodeCtx, GridLPChart)
    override fun <D : Dim<D>> ril_compile_pred(predicate: SPGP<*>): LPAffExpr<Int> {
        val (ix, pred) = predicate

        return when (pred) {
            // in these we rely on the fact that we have nice LPBinaries in the chart, which lets
            // us avoid a lot of clipping, etc.
            // TODO currently the typing requires LPAffExpr<Int> because we sometimes need to
            //  return a zero/one constant, creating it outside of a node context where we could
            //  e.g. make a bound LPBinary (which, too, would be suboptimal). We should make
            //  an LPConstant<N> : LPBounded<N> and then we can refine the type of the chart to
            //  LPBounded and statically check the bounds.
            is HasTile -> lptc[ix, pred.tile]
            is IsEntity -> entities[ix, pred.entity]
            is NoneForPlane -> {
                val tiles = ptc.tiles_of(pred.plane)
                val plane_sum = tiles.map { lptc[ix, it] }.lp_sum()
                RIL.not(plane_sum)
            }
        }
    }
}
