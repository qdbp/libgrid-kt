package kulp.transforms

import kulp.*
import kulp.constraints.LP_EQ
import kulp.constraints.LP_GEQ
import kulp.constraints.LP_LEQ
import kulp.variables.LPBinary
import model.LPName

// TODO can we reify constraints in a generic way?
/**
 * This class's output is a boolean variable that is true if and only if the two inputs are equal.
 *
 * One can think of this as a reified form of the LP_EQ constraint.
 *
 * Cost:
 * - 1 binary output
 * - 2 binary auxiliaries
 * - 5 constraints
 */
// TODO can we be more efficient?
class ReifiedEQ<N : Number>
private constructor(z_eq: LPBinary, val rhs: LPAffExpr<N>, val lhs: LPAffExpr<N>) :
    LPTransform<Int>(z_eq) {

    constructor(
        name: LPName,
        rhs: LPAffExpr<N>,
        lhs: LPAffExpr<N>
    ) : this(LPBinary(name), rhs, lhs)

    override fun LPName.render_auxiliaries(ctx: MipContext): List<LPRenderable> {
        val M = ctx.intM
        val diff = rhs - lhs

        val z_diff_neg = LPBinary(+"z_dlz") // diff less than zero
        val z_diff_pos = LPBinary(+"z_dgz") // diff greater than zero

        val constraints =
            listOf(
                // negative half, if diff < 0
                // we require z == 1 if diff < 0: -Mz       <= diff
                //            z == 0 if diff >=0:  M(1 - z) > diff <=> M(1 - z) - 1 >= diff
                LP_LEQ(z_diff_neg.name.refine("bind_1"), -M * z_diff_neg, diff),
                LP_GEQ(z_diff_neg.name.refine("bind_0"), M * !z_diff_neg - 1, diff),
                // positive half, if diff >= 0
                // we require z == 1 if diff > 0:  Mz        >= diff
                //            z == 0 if diff <=0: -M(1 - z)  < diff  <=> -M(1 - z) + 1 <= diff
                LP_GEQ(z_diff_pos.name.refine("bind_1"), M * z_diff_pos, diff),
                LP_LEQ(z_diff_pos.name.refine("bind_0"), -M * !z_diff_pos + 1, diff),
                // finally, pin our output
                LP_EQ(output.name.refine("bind"), output, 1 - z_diff_neg - z_diff_pos)
            )

        return constraints + listOf(z_diff_neg, z_diff_pos)
    }
}
