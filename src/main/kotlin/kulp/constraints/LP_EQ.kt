package kulp.constraints

import kulp.*
import model.SegName

class LP_EQ(override val name: SegName, lhs: LPExprLike, rhs: LPExprLike) : LPConstraint() {

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

    // standard form: lhs == 0
    private val std_lhs: LPAffineExpression = lhs.as_expr() - rhs.as_expr()

    // TODO add capability flags to MipContext which can be used to disable this
    override fun is_primitive(ctx: MipContext): Boolean = false

    override fun render(ctx: MipContext): List<LPRenderable> {
        return listOf(
            LP_LEQ(name.refine(constraint_identifier()).refine("leq_0"), std_lhs, 0),
            LP_LEQ(name.refine(constraint_identifier()).refine("geq_0"), -std_lhs, 0)
        )
    }
}
