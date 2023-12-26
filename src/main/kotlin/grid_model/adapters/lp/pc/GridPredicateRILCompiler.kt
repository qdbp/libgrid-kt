package grid_model.adapters.lp.pc

import grid_model.adapters.lp.GridLPChart
import grid_model.dimension.Dim
import grid_model.predicate.BaseGridPredicate
import grid_model.predicate.SPGP
import kulp.LPAffExpr
import kulp.NodeCtx

sealed interface GridPredicateRILCompiler<in P : BaseGridPredicate> {

    context(NodeCtx)
    fun <D: Dim<D>> ril_compile_pred(chart: GridLPChart, predicate: P): LPAffExpr<Int>
}

context(NodeCtx)
fun BaseGridPredicate.ril_compile(
    chart: GridLPChart,
): LPAffExpr<Int> =
    when (this) {
        is SPGP<*> -> PointPredicateRILCompiler.ril_compile_pred(chart, this)
        else -> throw NotImplementedError("No compiler for predicate $this")
    }
