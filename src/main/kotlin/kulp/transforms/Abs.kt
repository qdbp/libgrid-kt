package kulp.transforms

import kotlin.math.roundToInt
import kulp.LPAffExpr
import kulp.LPConstraint
import kulp.LPRenderable
import kulp.LPTransform
import kulp.MipContext
import kulp.constraints.LP_EQ
import kulp.constraints.LP_LEQ
import kulp.variables.LPBinary
import kulp.variables.LPInteger
import kulp.variables.LPReal
import kulp.variables.PrimitiveLPVariable
import model.LPName

sealed class Abs<N : Number>(y: PrimitiveLPVariable<N>, val x: LPAffExpr<N>) : LPTransform<N>(y) {

    /** We build constraints dynamically, since we need bigM to be known. */
    override fun LPName.render_auxiliaries(ctx: MipContext): List<LPRenderable> {
        // we can build our auxiliaries statically
        // override val name: SegName = x.name.refine(transform_identifier())
        val auxiliaries: MutableList<LPInteger> = mutableListOf()
        val x_p = LPInteger(+"p")
        val x_m = LPInteger(+"m")
        // z == 1 <=> x <= 0
        val z_x_is_neg = LPBinary(+"is_negative")
        auxiliaries.add(x_p)
        auxiliaries.add(x_m)

        val cxs: MutableList<LPConstraint> = mutableListOf()

        // split halves are positive
        cxs.add(LP_LEQ(+"xp_geq_0", 0, x_p))
        cxs.add(LP_LEQ(+"xm_geq_0", 0, x_m))
        // split halves actually represent the absolute value
        cxs.add(LP_EQ(+"xp_minus_xm_is_x", x_p - x_m, x))
        cxs.add(LP_EQ(+"y_eq_xm_plus_xp", output, x_m + x_p))
        // z == 1 <=> x <= 0
        val M = ctx.bigM.roundToInt()
        cxs.add(LP_LEQ(+"xm_z_switch", x_p, !z_x_is_neg * M))
        cxs.add(LP_LEQ(+"xp_z_switch", x_m, z_x_is_neg * M))
        return auxiliaries + cxs
    }
}

class IntAbs(name: LPName, x: LPAffExpr<Int>) : Abs<Int>(LPInteger(name), x)

class RealAbs(name: LPName, x: LPAffExpr<Double>) : Abs<Double>(LPReal(name), x)
