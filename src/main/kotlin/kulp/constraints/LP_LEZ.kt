package kulp.constraints

import kulp.BindCtx
import kulp.LPAffExpr
import kulp.LPConstraint
import kulp.LPNode

// TODO make normal form the only form, remove aux constructors
// TODO consider making generic versions of this for CP-SAT
//  under MIP we're ok casting all constraints to Double
/**
 * The buck stops here.
 *
 * This is the lowliest constraint in our universe, and its mighty shoulders bear the weight of
 * optimization universes.
 */
class LP_LEZ(node: LPNode, val lhs: LPAffExpr<*>) : LPConstraint(node) {
    companion object {
        context(BindCtx)
        operator fun invoke(lhs: LPAffExpr<*>) = LP_LEZ(take(), lhs)
    }

    override fun toString(): String = "[$lhs <= 0]"
}
