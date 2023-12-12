package kulp.aggregates

import kulp.BindCtx
import kulp.LPContext
import kulp.LPNode
import kulp.NodeCtx
import kulp.variables.LPBinary
import mdspan.NDSpan
import mdspan.lp_sum

/**
 * The archetypal aggregate!
 *
 * This class models the fundamental "one of N" constraint, where exactly one of the binary
 * variables must be true from among a set.
 *
 * It is backed by a multidimensional array of binary variables, and the constraint is applied to an
 * arbitrary subspace of the array. This flexibility means you should very rarely need to make lists
 * of this class.
 */
class LPOneOfN
private constructor(
    node: LPNode,
    // by default, we constrain on the last axis, since this tends to be how
    shape: List<Int>,
    constraint_subspace: List<Int> = listOf(shape.size - 1),
) : LPAggregate<LPBinary>(node, shape, { LPBinary() }, constraint_subspace) {

    companion object {
        // extremely cheesy workaround for KT-57183
        context(BindCtx)
        operator fun invoke(
            shape: List<Int>,
            constraint_subspace: List<Int> = listOf(shape.size - 1)
        ) = LPOneOfN(take(), shape, constraint_subspace)

        context(BindCtx)
        operator fun invoke(n: Int) = invoke(listOf(n))
    }

    init {
        require(constraint_subspace.isNotEmpty()) {
            "Creating with 0D constraint subspace -- this is just an array of LPBinaries."
        }
    }

    context(NodeCtx)
    override fun decompose_subarray(ctx: LPContext, subarray: NDSpan<LPBinary>) {
        "one_of_n" { subarray.lp_sum() eq 1 }
    }
}
