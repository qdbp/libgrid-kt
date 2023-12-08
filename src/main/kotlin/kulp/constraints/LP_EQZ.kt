package kulp.constraints

import kulp.*

// // TODO may want to parameterize for CP-SAT to force all-int constraints
// //  for now we project+relax
/**
 * LP_LEZ's slightly more pretentious cousin.
 *
 * In reality, depend on LP_LEZ for everything.
 */
class LP_EQZ(node: LPNode, val lhs: LPAffExpr<*>) : LPConstraint(node) {

    override fun decompose(ctx: LPContext) {
        node += lhs.lez named "lez"
        node += lhs.gez named "gez"
    }
}
