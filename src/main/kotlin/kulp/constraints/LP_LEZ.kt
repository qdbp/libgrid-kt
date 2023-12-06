package kulp.constraints

import kulp.LPAffExpr
import kulp.LPConstraint
import model.LPName

// TODO make normal form the only form, remove aux constructors
// TODO consider making generic versions of this for CP-SAT
//  under MIP we're ok casting all constraints to Double
/**
 * The buck stops here.
 *
 * This is the lowliest constraint in our universe, and its mighty shoulders bear the weight
 * of optimization universes.
 */
class LP_LEZ(override val name: LPName, val lhs: LPAffExpr<*>) : LPConstraint
