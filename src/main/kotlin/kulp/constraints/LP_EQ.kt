package kulp.constraints

import kulp.LPAffineExpression
import kulp.LPExprLike
import kulp.LPRenderable
import kulp.MipContext

class LP_EQ(override val name: String, lhs: LPExprLike, rhs: LPExprLike) : LPConstraint {

    constructor(
        name: String,
        lhs: Number,
        rhs: LPExprLike
    ) : this(name, LPAffineExpression(lhs), rhs)

    constructor(
        name: String,
        lhs: LPExprLike,
        rhs: Number
    ) : this(name, lhs, LPAffineExpression(rhs))

    // standard form: lhs == 0
    private val std_lhs: LPAffineExpression = lhs.as_expr() - rhs.as_expr()

    // TODO add capability flags to MipContext which can be used to disable this
    override fun is_primitive(ctx: MipContext): Boolean = false

    override fun render(ctx: MipContext): List<LPRenderable> {
        return listOf(LPLEQ("${name}_EQ_leq_0", std_lhs, 0), LPLEQ("${name}_EQ_geq_0", -std_lhs, 0))
    }
}
