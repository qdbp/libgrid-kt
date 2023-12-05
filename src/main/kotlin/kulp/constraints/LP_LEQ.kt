package kulp.constraints

import kulp.*
import model.LPName

// TODO consider making generic versions of this for CP-SAT
//  under MIP we're ok casting all constraints to Double
class LP_LEQ(override val name: LPName, lhs: LPAffExpr<*>, rhs: LPAffExpr<*>) : LPConstraint() {
    constructor(name: LPName, lhs: Number, rhs: LPAffExpr<*>) : this(name, RealAffExpr(lhs), rhs)

    constructor(name: LPName, lhs: LPAffExpr<*>, rhs: Number) : this(name, lhs, RealAffExpr(rhs))

    // standard form: lhs <= 0
    val std_lhs = lhs.relax() - rhs.relax()
}

// since GEQ is just a flipped LEQ there's no point making a separate class, we just make
// a function that flips the arguments before instantiating LEQ
fun LP_GEQ(name: LPName, lhs: LPAffExpr<*>, rhs: LPAffExpr<*>) = LP_LEQ(name, rhs, lhs)

fun LP_GEQ(name: LPName, lhs: Number, rhs: LPAffExpr<*>) = LP_LEQ(name, rhs, lhs)

fun LP_GEQ(name: LPName, lhs: LPAffExpr<*>, rhs: Number) = LP_LEQ(name, rhs, lhs)
