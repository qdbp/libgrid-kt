package kulp.constraints

import kulp.LPAffExpr
import kulp.LPConstraint
import kulp.LPContext
import kulp.LPRenderable
import model.LPName

/**
 * Did I mention I gave these ugly names to encourage direct instantiation?
 *
 * The builder-style methods on LPAffExpr should be strongly preferred.
 */
// TODO yadda yadda generic ints cpsat
class LP_BND(
    override val name: LPName,
    val expr: LPAffExpr<*>,
    val lb: Number?,
    val ub: Number?
) : LPConstraint {

    override fun LPName.decompose(ctx: LPContext): List<LPRenderable> {
        return listOfNotNull(
            lb?.let { expr ge it named "lb" },
            ub?.let { expr le it named "ub" },
        )
    }
}
