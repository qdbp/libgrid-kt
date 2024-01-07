package grid_model.adapters.lp.ril_compile

import grid_model.adapters.lp.GridLPChart
import grid_model.geom.Dim
import grid_model.predicate.*
import kulp.LPAffExpr
import kulp.NodeCtx
import kulp.lp_sum
import kulp.ril.RIL

data object PointPredicateRILCompiler : GridPredicateRILCompiler<LPP<*>> {
    /**
     * Converts a predicate of the given type P to a RIL-expression that witnesses its truth.
     *
     * The ril expression should be an integer affine expr s.t. expr >= 1 <=> P is satisfied.
     *
     * See [RIL] for details.
     */
    context(NodeCtx, GridLPChart)
    override fun <D : Dim<D>> ril_compile_pred(predicate: LPP<*>): LPAffExpr<Int> {
        val (ix, pred) = predicate

        val out =  when (pred) {
            // in these we rely on the fact that we have nice LPBinaries in the chart, which lets
            // us avoid a lot of clipping, etc.
            // TODO currently the typing requires LPAffExpr<Int> because we sometimes need to
            //  return a zero/one constant, creating it outside of a node context where we could
            //  e.g. make a bound LPBinary (which, too, would be suboptimal). We should make
            //  an LPConstant<N> : LPBounded<N> and then we can refine the type of the chart to
            //  LPBounded and statically check the bounds.
            is HasAnyTile -> {
                pred.tiles.map { lptc[ix, index.plane_of(it), it].to_lp_masked_zero() }.lp_sum()
            }
            is HasTileOrMasked -> lptc[ix, index.plane_of(pred.tile), pred.tile].to_lp_masked_one()
            is HasAnyEntity -> pred.entities.map { lpec[ix, it].to_lp_masked_zero() }.lp_sum()
            is HasEntityOrMasked -> lpec[ix, pred.entity].to_lp_masked_one()
            is NoneForPlane -> {
                val tiles = index.tiles_of(pred.plane)
                val plane_sum = tiles.map { lptc[ix, pred.plane, it].to_lp_masked_one() }.lp_sum()
                RIL.not(plane_sum)
            }
        }
        return out
    }
}
