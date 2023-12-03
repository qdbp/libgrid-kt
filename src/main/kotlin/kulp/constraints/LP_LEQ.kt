package kulp.constraints

import kulp.*
import model.SegName

// TODO consider making generic versions of this for CP-SAT
//  under MIP we're ok casting all constraints to Double
class LP_LEQ(override val name: SegName, lhs: LPAffExpr<*>, rhs: LPAffExpr<*>) : LPConstraint() {
    constructor(name: SegName, lhs: Number, rhs: LPAffExpr<*>) : this(name, RealAffExpr(lhs), rhs)

    constructor(name: SegName, lhs: LPAffExpr<*>, rhs: Number) : this(name, lhs, RealAffExpr(rhs))

    // standard form: lhs <= 0
    val std_lhs = lhs.relax() - rhs.relax()

    /** Simple inequality constraints are primitive for all known modes. */
    override fun is_primitive(ctx: MipContext): Boolean = true

    override fun render(ctx: MipContext): List<LPRenderable> = listOf(this)
}

fun LP_GEQ(name: SegName, lhs: LPAffExpr<*>, rhs: LPAffExpr<*>) = LP_LEQ(name, rhs, lhs)

fun LP_GEQ(name: SegName, lhs: Number, rhs: LPAffExpr<*>) = LP_LEQ(name, rhs, lhs)

fun LP_GEQ(name: SegName, lhs: LPAffExpr<*>, rhs: Number) = LP_LEQ(name, rhs, lhs)
