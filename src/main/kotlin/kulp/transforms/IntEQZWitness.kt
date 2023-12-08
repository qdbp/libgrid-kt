package kulp.transforms

import kulp.*
import kulp.variables.BaseLPInteger
import kulp.variables.LPBinary

// TODO can we reify constraints in a generic way?
/**
 * This class's output is a boolean variable that is true if and only if the input is equal to zero.
 *
 * One can think of this as a reified form of the LP_EQ constraint.
 *
 * Cost:
 * - 1 binary output
 * - 2 binary auxiliaries
 * - 5 constraints
 */
// TODO can we be more efficient?
// TODO can we express this for Reals? the trouble is the M(1 - z) > x term...
class IntEQZWitness(node: LPNode, val x: LPAffExpr<Int>) : BaseLPInteger(node) {
    override val lb = 0
    override val ub = null

    constructor(node: LPNode, lhs: LPAffExpr<Int>, rhs: LPAffExpr<Int>) : this(node, lhs - rhs)

    override fun decompose(ctx: LPContext) {
        require(ctx is BigMCapability)
        val M = ctx.intM
        // diff less than zero
        val zn = node grow { LPBinary(it) } named "z_dlz"
        // diff greater than zero
        val zp = node grow { LPBinary(it) } named "z_dgz"
        // negative half, if x < 0
        // we require z == 1 if x < 0: -Mz       <= x
        zn.node += -M * zn le x named "bind_1"
        //            z == 0 if x >= 0:  M(1 - z)  > x
        zn.node += M * !zn gt x named "bind_0"
        // positive half, if x >= 0
        // we require z == 1 if x >  0:  Mz        >= x
        zp.node += M * zp ge x named "bind_1"
        //            z == 0 if x <= 0: -M(1 - z)  < x
        zp.node += -M * !zp lt x named "bind_0"

        node += this eq (1 - zn - zp) named "bind"
    }
}
