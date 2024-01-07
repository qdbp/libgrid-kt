package kulp.constraints

import kulp.*

// // TODO may want to parameterize for CP-SAT to force all-int constraints
// //  for now we project+relax
/**
 * LP_LEZ's slightly more pretentious cousin.
 *
 * In reality, depend on LP_LEZ for everything.
 */
class LP_EQZ private constructor(node: LPNode, val lhs: LPAffExpr<*>) : LPConstraint(node) {

    companion object {
        context(BindCtx)
        operator fun invoke(lhs: LPAffExpr<*>) = LP_EQZ(take(), lhs)
    }

    context(NodeCtx)
    override fun decompose(ctx: LPContext) {
        "lez" { lhs.lez }
        "gez" { lhs.gez }
    }
}
