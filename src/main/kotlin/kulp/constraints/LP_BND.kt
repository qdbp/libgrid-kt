package kulp.constraints

import kulp.*

/**
 * Did I mention I gave these ugly names to discourage direct instantiation?
 *
 * The builder-style methods on LPAffExpr should be strongly preferred.
 */
// TODO yadda yadda generic ints cpsat
class LP_BND
private constructor(
    node: LPNode,
    private val expr: LPAffExpr<*>,
    val lb: Number?,
    val ub: Number?
) : LPConstraint(node) {

    companion object {
        context(BindCtx)
        operator fun invoke(expr: LPAffExpr<*>, lb: Number? = null, ub: Number? = null): LP_BND =
            LP_BND(take(), expr, lb, ub)
    }

    context(NodeCtx)
    override fun decompose(ctx: LPContext) {
        lb?.let { "lb" { expr ge it } }
        ub?.let { "ub" { expr le it } }
    }
}
