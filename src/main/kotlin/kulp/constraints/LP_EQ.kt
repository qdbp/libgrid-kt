package kulp.constraints

import kulp.*
import model.LPName

// TODO may want to parameterize for CP-SAT to force all-int constraints
//  for now we project+relax
class LP_EQ(override val name: LPName, lhs: LPAffExpr<*>, rhs: LPAffExpr<*>) : LPConstraint() {

    constructor(name: LPName, lhs: Number, rhs: LPAffExpr<*>) : this(name, RealAffExpr(lhs), rhs)

    constructor(name: LPName, lhs: LPAffExpr<*>, rhs: Number) : this(name, lhs, RealAffExpr(rhs))

    // standard form: lhs == 0
    private val std_lhs: LPAffExpr<Double> = lhs.relax() - rhs.relax()

    override fun LPName.decompose(ctx: MipContext): List<LPRenderable> {
        return listOf(
            LP_LEQ(+"lez", std_lhs, 0),
            LP_LEQ(+"gez", -std_lhs, 0),
        )
    }
}
