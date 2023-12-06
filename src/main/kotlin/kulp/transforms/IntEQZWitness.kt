package kulp.transforms

import kulp.*
import kulp.variables.LPBinary
import model.LPName

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
class IntEQZWitness private constructor(z_eq: LPBinary, val x: LPAffExpr<Int>) :
    LPTransform<Int>(z_eq) {

    constructor(name: LPName, term: LPAffExpr<Int>) : this(LPBinary(name), term)

    constructor(
        name: LPName,
        lhs: LPAffExpr<Int>,
        rhs: LPAffExpr<Int>
    ) : this(LPBinary(name), lhs - rhs)

    override fun LPName.render_auxiliaries(ctx: LPContext): List<LPRenderable> {
        require(ctx is BigMCapability)
        val M = ctx.intM

        val zn = LPBinary(+"z_dlz") // diff less than zero
        val zp = LPBinary(+"z_dgz") // diff greater than zero

        val constraints = mutableListOf<LPRenderable>()
        with(zn.name) {
            // negative half, if x < 0
            // we require z == 1 if x < 0: -Mz       <= x
            constraints += -M * zn le x named "bind_1"
            //            z == 0 if x >= 0:  M(1 - z)  > x
            constraints += M * !zn gt x named "bind_0"
        }
        // positive half, if x >= 0
        with(zp.name) {
            // we require z == 1 if x >  0:  Mz        >= x
            constraints += M * zp ge x named "bind_1"
            //            z == 0 if x <= 0: -M(1 - z)  < x
            constraints += -M * !zp lt x named "bind_0"
            // LP_EQZ(output.name.refine("bind"), output, 1 - zn - zp)
        }
        constraints += output eq (1 - zn - zp) named "bind"
        return constraints + listOf(zn, zp)
    }
}
