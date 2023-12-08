package kulp.constraints

import kulp.*

/**
 * Did I mention I gave these ugly names to encourage direct instantiation?
 *
 * The builder-style methods on LPAffExpr should be strongly preferred.
 */
// TODO yadda yadda generic ints cpsat
class LP_BND(node: LPNode, val expr: LPAffExpr<*>, val lb: Number?, val ub: Number?) :
    LPConstraint(node) {

    override fun decompose(ctx: LPContext) {
        lb?.let { node += expr ge it named "lb" }
        ub?.let { node += expr le it named "ub" }
    }
}
