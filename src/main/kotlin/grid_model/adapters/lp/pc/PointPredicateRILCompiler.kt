package grid_model.adapters.lp.pc

import grid_model.adapters.lp.GridLPChart
import grid_model.predicate.HasTile
import grid_model.predicate.IsEntity
import grid_model.predicate.NoneForPlane
import grid_model.predicate.SPGP
import kulp.LPAffExpr
import kulp.NodeCtx
import kulp.lp_sum
import kulp.transforms.ril.RIL

object PointPredicateRILCompiler : GridPredicateRILCompiler<SPGP> {
    /**
     * Converts a predicate of the given type P to a RIL-expression that witnesses its truth.
     *
     * The ril expression should be an integer affine expr s.t. expr >= 1 <=> P is satisfied.
     */
    context(NodeCtx)
    override fun ril_compile_pred(chart: GridLPChart, predicate: SPGP): LPAffExpr<Int> {
        val (ix, pred) = predicate
        return when (pred) {
            // in these we use the fact that we have nice LPBinaries in the chart, which lets
            // us avoid a lot of clipping, etc.
            // we use `put` instead of `use` here to give these some names
            is HasTile -> chart.lptc[ix, pred.tile]
            is IsEntity -> chart.entities[ix, pred.entity]
            is NoneForPlane -> {
                val tiles = chart.ptc.tiles_of(pred.plane)
                val plane_sum = tiles.map { chart.lptc[ix, it] }.lp_sum()
                RIL.not(plane_sum)
            }
        }
    }
}
