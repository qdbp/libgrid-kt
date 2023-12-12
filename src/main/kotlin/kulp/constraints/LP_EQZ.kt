package kulp.constraints

import kulp.*
import kulp.BindCtx

// // TODO may want to parameterize for CP-SAT to force all-int constraints
// //  for now we project+relax
/**
 * LP_LEZ's slightly more pretentious cousin.
 *
 * In reality, depend on LP_LEZ for everything.
 */
context(BindCtx)
class LP_EQZ(val lhs: LPAffExpr<*>) : LPConstraint() {

    context(NodeCtx)
    override fun decompose(ctx: LPContext) {
        "lez" { lhs.lez }
        "gez" { lhs.gez }
    }
}
