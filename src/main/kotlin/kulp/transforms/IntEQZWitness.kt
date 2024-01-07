package kulp.transforms

import kulp.*
import kulp.expressions.gt
import kulp.expressions.lt
import kulp.variables.LPVar

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
class IntEQZWitness private constructor(self: LPVar<Int>, val x: LPAffExpr<Int>) :
    LPTransform<Int>(self) {

    companion object {
        // workaround for KT-57183
        context(BindCtx)
        operator fun invoke(x: LPAffExpr<Int>): IntEQZWitness = IntEQZWitness(self_by_example(x), x)

        context(BindCtx)
        operator fun invoke(x: LPAffExpr<Int>, y: LPAffExpr<Int>): IntEQZWitness =
            IntEQZWitness(self_by_example(x, lb = x.dom.zero), x - y)
    }

    context(NodeCtx)
    override fun decompose(ctx: LPContext) {
        require(ctx is BigMCapability)
        val M = ctx.intM
        // case x < 0
        val zn =
            "z_dlz".new_binary().branch {
                // z == 1 if x < 0: -Mz       <= x
                "bind_1" { -M * it le x }
                // z == 0 if x >= 0:  M(1 - z)  > x
                "bind_0" { M * !it gt x }
            }
        // case x >= 0
        val zp =
            "z_dgz".new_binary().branch {
                // z == 1 if x >  0:  Mz        >= x
                "bind_1" { M * it ge x }
                // z == 0 if x <= 0: -M(1 - z)  < x
                "bind_0" { -M * !it lt x }
            }
        "bind" { y eq (1 - zn - zp) }
    }
}
