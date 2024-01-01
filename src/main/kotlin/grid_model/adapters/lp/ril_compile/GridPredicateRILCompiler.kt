package grid_model.adapters.lp.ril_compile

import grid_model.adapters.lp.GridLPChart
import grid_model.dimension.Dim
import grid_model.predicate.GridPredicate
import grid_model.predicate.SPGP
import kulp.LPAffExpr
import kulp.NodeCtx

sealed interface GridPredicateRILCompiler<in P : GridPredicate> {

    context(NodeCtx, GridLPChart)
    fun <D : Dim<D>> ril_compile_pred(predicate: P): LPAffExpr<Int>

    companion object {
        context(NodeCtx, GridLPChart)
        fun GridPredicate.ril_compile(): LPAffExpr<Int> =
            when (this) {
                is SPGP<*> -> PointPredicateRILCompiler.ril_compile_pred(this)
                else -> throw NotImplementedError("No compiler for predicate $this")
            }
    }
}
