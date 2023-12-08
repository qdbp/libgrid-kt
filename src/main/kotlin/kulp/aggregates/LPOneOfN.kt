package kulp.aggregates

import kulp.LPAggregate
import kulp.LPContext
import kulp.LPNode
import kulp.LPRenderable
import kulp.variables.LPBinary
import mdspan.NDSpan
import mdspan.NDSpanImpl
import mdspan.lp_sum

/**
 * The archetypal aggregate!
 *
 * This class models the fundamental "one of N" constraint, where exactly one of the binary
 * variables must be true.
 */
class LPOneOfN(
    node: LPNode,
    vars: NDSpan<LPBinary>,
    // by default, we constrain on the last axis, since this tends to be how
    constraint_subspace: List<Int> = listOf(vars.shape.size - 1),
) : LPAggregate<LPBinary>(node, vars, constraint_subspace) {

    constructor(
        node: LPNode,
        shape: List<Int>,
        constraint_subspace: List<Int> = listOf(shape.size - 1),
    ) : this(
        node,
        NDSpanImpl.full_by(shape) { ndix -> node grow { LPBinary(it) } named ndix },
        constraint_subspace
    )

    constructor(node: LPNode, n: Int) : this(node, listOf(n))

    override fun decompose_subarray(
        ctx: LPContext,
        subspace_node: LPNode,
        subarray: NDSpan<LPBinary>
    ): LPRenderable {
        val sum = subarray.lp_sum()
        val lp_eq = subspace_node grow (sum eq 1) named "one_of_n_decompose_subarr"
        println("XXXXXXXXXXXXXXXXXXXXXX$lp_eq")
        return lp_eq
    }
}
