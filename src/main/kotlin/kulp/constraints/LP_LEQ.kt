package kulp.constraints

import kulp.*

class LP_LEQ(override val name: LPName, lhs: LPExprLike, rhs: LPExprLike = LPAffineExpression()) : LPConstraint() {
    constructor(name: LPName, lhs: Number, rhs: LPExprLike) : this(name, LPAffineExpression(lhs), rhs)
    constructor(name: LPName, lhs: LPExprLike, rhs: Number) : this(name, lhs, LPAffineExpression(rhs))

    // standard form: lhs <= 0
    val std_lhs: LPAffineExpression = lhs.as_expr() - rhs.as_expr()

    /**
     * Simple inequality constraints are primitive for all known modes.
     */
    override fun is_primitive(ctx: MipContext): Boolean  = true

    override fun render(ctx: MipContext): List<LPRenderable> = listOf(this)
}
