package kulp

import kulp.variables.LPVar
import mdspan.NDSpan

/**
 * An aggregate is a structured multidimensional array of other variables.
 *
 * It is not a renderable, but produces a renderable per subspace.
 */
abstract class LPAggregate<V : LPVar<*>>(
    node: LPNode,
    val arr: NDSpan<V>,
    private val constraint_subspace: List<Int>
) : LPRenderable(node), NDSpan<V> by arr {

    final override fun decompose(ctx: LPContext) {
        arr.apply_subspace_indexed(constraint_subspace) { ndix, subarray ->
            node grow { sub_node -> decompose_subarray(ctx, sub_node, subarray) } named ndix
        }
    }

    abstract fun decompose_subarray(
        ctx: LPContext,
        subspace_node: LPNode,
        subarray: NDSpan<V>,
    ): LPRenderable
}
