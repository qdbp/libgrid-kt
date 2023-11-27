package kulp.constraints

import kulp.*
import model.SegName

class LP_LEQ(override val name: SegName, lhs: LPExprLike, rhs: LPExprLike = LPAffineExpression()) :
    LPConstraint() {
    constructor(
        name: SegName,
        lhs: Number,
        rhs: LPExprLike
    ) : this(name, LPAffineExpression(lhs), rhs)

    constructor(
        name: SegName,
        lhs: LPExprLike,
        rhs: Number
    ) : this(name, lhs, LPAffineExpression(rhs))

    // standard form: lhs <= 0
    val std_lhs: LPAffineExpression = lhs.as_expr() - rhs.as_expr()

    /** Simple inequality constraints are primitive for all known modes. */
    override fun is_primitive(ctx: MipContext): Boolean = true

    override fun render(ctx: MipContext): List<LPRenderable> = listOf(this)
}

fun LP_GEQ(name: SegName, lhs: LPExprLike, rhs: LPExprLike) = LP_LEQ(name, rhs, lhs)
