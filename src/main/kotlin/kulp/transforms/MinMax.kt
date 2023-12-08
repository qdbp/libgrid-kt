package kulp.transforms

import kulp.*
import kulp.aggregates.LPOneOfN
import kulp.variables.LPVar

/** Private base class for IntMin and IntMax. */
sealed class MinMax<N : Number>(
    node: LPNode,
    val vars: List<LPAffExpr<N>>,
    private val which: Which
    // this will crash if vars is empty, but that's fine, it's undefined regardless in that case
) : LPTransform<N>(node, vars[0].domain) {

    override val lb: N? = null
    override val ub: N? = null

    protected enum class Which {
        min,
        max
    }

    override fun decompose_auxiliaries(node: LPNode, out: LPVar<N>, ctx: LPContext) {
        require(ctx is BigMCapability)
        val M = ctx.bigM

        val selector = node grow { LPOneOfN(it, vars.size) } named "bind_sel"

        // standard max formulation:
        // for all i: y >= x_i
        // exists j : y <= x_j
        // selector picks out j
        for (ix in vars.indices) {
            when (which) {
                // we can relax the comparisons for bigM since rounding error, by construction,
                // doesn't matter
                Which.max -> {
                    node += out ge vars[ix] named "ge_bind_$ix"
                    node +=
                        out le vars[ix].relax() + M * (!selector.arr[ix]).relax() named "le_bind_$ix"
                }
                Which.min -> {
                    node += out le vars[ix] named "le_bind_$ix"
                    node +=
                        out ge vars[ix].relax() - M * (!selector.arr[ix]).relax() named "ge_bind_$ix"
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
class Min<N : Number>(name: LPNode, vars: List<LPAffExpr<N>>) : MinMax<N>(name, vars, Which.min)

/**
 * Returns a variable always equal to the maximum of the input variables.
 *
 * Auxiliaries: |vars| 1-of-N binary set Outputs: 1 Integer output variable
 */
class Max<N : Number>(name: LPNode, vars: List<LPAffExpr<N>>) : MinMax<N>(name, vars, Which.max)
