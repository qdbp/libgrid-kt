package kulp.constraints

import kulp.BindCtx
import kulp.LPAffExpr
import kulp.LPConstraint

// TODO make normal form the only form, remove aux constructors
// TODO consider making generic versions of this for CP-SAT
//  under MIP we're ok casting all constraints to Double
/**
 * The buck stops here.
 *
 * This is the lowliest constraint in our universe, and its mighty shoulders bear the weight of
 * optimization universes.
 */
context(BindCtx)
class LP_LEZ(val lhs: LPAffExpr<*>) : LPConstraint() {
    override fun toString(): String = "[$lhs <= 0]"
}
