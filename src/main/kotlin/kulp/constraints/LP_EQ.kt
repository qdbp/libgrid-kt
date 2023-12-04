package kulp.constraints

import kulp.*
import model.SegName

// TODO may want to parameterize for CP-SAT to force all-int constraints
//  for now we project+relax
class LP_EQ(override val name: SegName, lhs: LPAffExpr<*>, rhs: LPAffExpr<*>) : LPConstraint() {

    constructor(name: SegName, lhs: Number, rhs: LPAffExpr<*>) : this(name, RealAffExpr(lhs), rhs)

    constructor(name: SegName, lhs: LPAffExpr<*>, rhs: Number) : this(name, lhs, RealAffExpr(rhs))

    // standard form: lhs == 0
    private val std_lhs: LPAffExpr<Double> = lhs.relax() - rhs.relax()

    // TODO add capability flags to MipContext which can be used to disable this
    override fun is_primitive(ctx: MipContext): Boolean = false

    override fun render(ctx: MipContext): List<LPRenderable> {
        return listOf(
            LP_LEQ(name.refine(constraint_identifier()).refine("lez"), std_lhs, 0),
            LP_LEQ(name.refine(constraint_identifier()).refine("gez"), -std_lhs, 0)
        )
    }
}
