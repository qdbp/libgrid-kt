package grid_model.adapters.lp.pc

import grid_model.adapters.lp.GridLPChart
import grid_model.dimension.Dim
import grid_model.predicate.BaseGridPredicate
import grid_model.predicate.SPGP
import kulp.LPAffExpr
import kulp.NodeCtx

sealed interface GridPredicateRILCompiler<in P : BaseGridPredicate> {

    context(NodeCtx, GridLPChart)
    fun <D : Dim<D>> ril_compile_pred(predicate: P): LPAffExpr<Int>
}

context(NodeCtx, GridLPChart)
fun BaseGridPredicate.ril_compile(): LPAffExpr<Int> =
    when (this) {
        is SPGP<*> -> PointPredicateRILCompiler.ril_compile_pred(this)
        else -> throw NotImplementedError("No compiler for predicate $this")
    }
