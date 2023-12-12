package kulp

import kotlin.reflect.KClass
import kulp.variables.LPVar

interface LPDomain<N : Number> {
    val klass: KClass<N>
    val zero: N
    val one: N

    context(BindCtx)
    fun newvar(lb: N? = null, ub: N? = null): LPVar<N>

    /**
     * this is a dangerous function that can lead to nonsense if misused (aka making an affine
     * expression of constraints). However, we need this in some very particular places to avoid
     * type erasure pain.
     */
    fun unsafe_node_as_expr(node: LPNode): LPAffExpr<N>

    /**
     * Things are never happy when there's a function called "coerce" in the offing...
     *
     * However, we keep all the quite static and unchanging implementation details of when this is
     * used deeply under control in [LPAffExpr].
     *
     * This function should never need to be called by implementing classes directly.
     */
    fun coerce(expr: LPAffExpr<*>): LPAffExpr<N>

    fun coerce_number(n: Number): N

    val max: (N, N) -> N
    val min: (N, N) -> N

    // not operators, no
    val mul: (N, N) -> N
    val add: (N, N) -> N
}
