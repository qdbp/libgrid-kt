package kulp.constraints

import kulp.*

/**
 * Did I mention I gave these ugly names to discourage direct instantiation?
 *
 * The builder-style methods on LPAffExpr should be strongly preferred.
 */
// TODO yadda yadda generic ints cpsat
context(BindCtx)
class LP_BND(private val expr: LPAffExpr<*>, val lb: Number?, val ub: Number?) : LPConstraint() {

    context(NodeCtx)
    override fun decompose(ctx: LPContext) {
        lb?.let { "lb" { expr ge it } }
        ub?.let { "ub" { expr le it } }
    }
}
