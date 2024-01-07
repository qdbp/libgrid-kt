package kulp.aggregates

import kulp.*
import kulp.expressions.LPBinaryExpr
import kulp.expressions.Zero
import mdspan.NDSlice
import mdspan.NDSpan
import mdspan.SumIX
import mdspan.lp_sum

/**
 * The archetypal aggregate!
 *
 * This class models the fundamental "one of N" constraint, where exactly one of the binary
 * variables must be true from among a set.
 *
 * It is backed by a multidimensional array of binary expressions, and the constraint is applied to
 * an arbitrary subspace of the array. This flexibility means you should very rarely need to make
 * lists of this class.
 */
class LPOneOfN
private constructor(
    node: LPNode,
    // by default, we constrain on the last axis, since this tends to be how
    shape: List<Int>,
    constraint_subspace: List<Int>,
    private val mask: NDSlice?,
) : LPAggregate<LPBinaryExpr>(node, shape, { create_var(it, mask) }, constraint_subspace) {

    companion object {
        context(NodeCtx)
        private fun create_var(ndix: List<Int>, mask: NDSlice?): LPBinaryExpr {
            return when {
                mask != null && mask.contains(ndix) -> Zero
                else -> ndix.new_binary().lift01()
            }
        }

        // extremely cheesy workaround for KT-57183
        /**
         * The public (synthetic) constructor.
         *
         * @param shape the shape of the array of variables to create
         * @param constraint_subspace the subspace of the array to apply the constraint to.
         *   Examples: in a (3, 4, 5) shaped array, setting this to [2] will force the sum of the 5
         *   variables along the last axis to be 1 (the total sum of the array will be 12). Setting
         *   it to [0, 2] will force the sum of the 15 variables along the first and last axes to be
         *   1 (the total sum of the array will be 4)
         * @param mask a set of array nd-indices where a constant zero should be used instead of a
         *   variable. This can be used to zero-out e.g. known-impossible combinations without
         *   creating useless variables and constraints for them.
         */
        context(BindCtx)
        operator fun invoke(
            shape: List<Int>,
            constraint_subspace: List<Int> = listOf(shape.size - 1),
            mask: Collection<List<Int>> = listOf(),
        ) = LPOneOfN(take(), shape, constraint_subspace, SumIX.of_points(mask.toList()))

        context(BindCtx)
        operator fun invoke(n: Int) = invoke(listOf(n))
    }

    init {
        require(constraint_subspace.isNotEmpty()) {
            "Creating with 0D constraint subspace -- this is just an array of LPBinaries."
        }

        // check that we don't have a fully masked subarray -- this would be immediately infeasible
        arr.apply_subspace(constraint_subspace) {
            require(it.lp_sum().terms.isNotEmpty()) {
                throw ProvenInfeasible(
                    "LPOneOfN constraint on fully masked subarray, can never sum to 1."
                )
            }
        }
    }

    context(NodeCtx)
    override fun decompose_subarray(ctx: LPContext, subarray: NDSpan<LPBinaryExpr>) {
        "one_of_n" { subarray.lp_sum() eq 1 }
    }
}
