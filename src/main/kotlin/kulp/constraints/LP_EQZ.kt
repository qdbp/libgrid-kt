package kulp.constraints

import kulp.*
import model.LPName

// // TODO may want to parameterize for CP-SAT to force all-int constraints
// //  for now we project+relax
/**
 * LP_LEZ's slightly more pretentious cousin.
 *
 * In reality, depend on LP_LEZ for everything.
 */
class LP_EQZ(override val name: LPName, val lhs: LPAffExpr<*>) : LPConstraint {

    override fun LPName.decompose(ctx: LPContext): List<LPRenderable> =
        listOf(
            lhs.lez() named "lez",
            lhs.gez() named "gez",
        )
}
