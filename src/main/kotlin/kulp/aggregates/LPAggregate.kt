package kulp.aggregates

import kulp.*
import mdspan.NDSpan
import mdspan.NDSpanImpl

/**
 * An aggregate is a structured multidimensional array of other variables.
 *
 * Its behavior as a Renderable must be as follows:
 * - the individual component variables are placed onto the tree eagerly irrespective of whether it
 *   is considered primitive or not. This is necessary for them to be used in expressions. Any
 *   adapter implementing this class as a primitive must take this into account.
 * - any other renderables are placed only when the aggregate is decomposed. This includes any
 *   constraints (such as sum-to-one for [kulp.aggregates.LPOneOfN]) that are applied to the
 *   aggregate, as well as any auxiliaries that are not the grid variables themselves.
 */
abstract class LPAggregate<E : LPAffExpr<*>>
private constructor(
    override val node: LPNode,
    val arr: NDSpan<E>,
    protected val constraint_subspace: List<Int>
) : LPRenderable, NDSpan<E> by arr {

    protected constructor(
        node: LPNode,
        shape: List<Int>,
        definition: (NodeCtx).(List<Int>) -> E,
        constraint_subspace: List<Int> = listOf(shape.size - 1),
    ) : this(
        node,
        NDSpanImpl.full_by(shape) { ndix -> node { definition(ndix) } },
        constraint_subspace
    )

    context(NodeCtx)
    final override fun decompose(ctx: LPContext) {
        arr.apply_subspace_indexed(constraint_subspace) { ndix, subarray ->
            branch(ndix.lp_name) { decompose_subarray(ctx, subarray) }
        }
    }

    context(NodeCtx)
    abstract fun decompose_subarray(ctx: LPContext, subarray: NDSpan<E>)
}
