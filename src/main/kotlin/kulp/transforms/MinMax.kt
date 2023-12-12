package kulp.transforms

import kulp.*
import kulp.aggregates.LPOneOfN
import kulp.variables.LPVar

/** Private base class for IntMin and IntMax. */
sealed class MinMax<N : Number>(
    self: LPVar<N>,
    val vars: List<LPAffExpr<N>>,
    private val which: Which,
    // this will crash if vars is empty, but that's fine, it's undefined regardless in that case
) : LPTransform<N>(self) {

    protected enum class Which {
        min,
        max
    }

    context(NodeCtx)
    override fun decompose(ctx: LPContext) {
        require(ctx is BigMCapability)
        val M = ctx.bigM

        val selector = "bind_sel" { LPOneOfN(vars.size) }

        // standard max formulation:
        // for all i: y >= x_i
        // exists j : y <= x_j
        // selector picks out j
        for (ix in vars.indices) {
            when (which) {
                // we can relax the comparisons for bigM since rounding error, by construction,
                // doesn't matter
                Which.max -> {
                    "ge_bind_$ix" { y ge vars[ix] }
                    "le_bind_$ix" { y le vars[ix].relax() + M * (!selector.arr[ix]).relax() }
                }
                Which.min -> {
                    "le_bind_$ix" { y le vars[ix] }
                    "ge_bind_$ix" { y ge vars[ix].relax() - M * (!selector.arr[ix]).relax() }
                }
            }
        }
    }
}

/**
 * Returns a variable always equal to the minimum of the input variables.
 *
 * Auxiliaries: |vars| 1-of-N binary set Outputs: 1 Integer output variable
 */
context(BindCtx)
class Min<N : Number> private constructor(self: LPVar<N>, vars: List<LPAffExpr<N>>) :
    MinMax<N>(self, vars, Which.min) {
    companion object {
        // workaround for KT-57183
        context(BindCtx)
        operator fun invoke(vars: List<LPAffExpr<Int>>): Min<Int> =
            Min(self_by_example(vars[0]), vars)
    }
}

/**
 * Returns a variable always equal to the maximum of the input variables.
 *
 * Auxiliaries: |vars| 1-of-N binary set Outputs: 1 Integer output variable
 */
context(BindCtx)
class Max<N : Number> private constructor(self: LPVar<N>, vars: List<LPAffExpr<N>>) :
    MinMax<N>(self, vars, Which.max) {
    companion object {
        // workaround for KT-57183
        context(BindCtx)
        operator fun invoke(vars: List<LPAffExpr<Int>>): Max<Int> =
            Max(self_by_example(vars[0]), vars)
    }
}
