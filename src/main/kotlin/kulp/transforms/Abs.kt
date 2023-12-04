package kulp.transforms

import kulp.LPAffExpr
import kulp.LPRenderable
import kulp.LPTransform
import kulp.MipContext
import kulp.constraints.LPConstraint
import kulp.constraints.LP_EQ
import kulp.constraints.LP_LEQ
import kulp.variables.LPBinary
import kulp.variables.LPInteger
import kulp.variables.LPReal
import kulp.variables.PrimitiveLPVariable
import model.SegName
import kotlin.math.roundToInt

sealed class Abs<N : Number>(y: PrimitiveLPVariable<N>, val x: LPAffExpr<N>) :
    LPTransform<N>(y) {

    override fun is_primitive(ctx: MipContext): Boolean = false

    /** We build constraints dynamically, since we need bigM to be known. */
    override fun render_auxiliaries(ctx: MipContext): List<LPRenderable> {
        // we can build our auxiliaries statically
        val auxiliaries: MutableList<LPInteger> = mutableListOf()
        // override val name: SegName = x.name.refine(transform_identifier())

        val x_p = LPInteger(name.refine("p"))
        val x_m = LPInteger(name.refine("m"))

        // z == 1 <=> x <= 0
        val z_x_is_neg = LPBinary(name.refine("is_negative"))

        auxiliaries.add(x_p)
        auxiliaries.add(x_m)
        val cxs: MutableList<LPConstraint> = mutableListOf()

        // split halves are positive
        cxs.add(LP_LEQ(name.refine("xp_geq_0"), 0, x_p))
        cxs.add(LP_LEQ(name.refine("xm_geq_0"), 0, x_m))

        // split halves actually represent the absolute value
        cxs.add(LP_EQ(name.refine("xp_minus_xm_is_x"), x_p - x_m, x))
        cxs.add(LP_EQ(name.refine("y_eq_xm_plus_xp"), output, x_m + x_p))

        // z == 1 <=> x <= 0
        val M = ctx.bigM.roundToInt()
        cxs.add(LP_LEQ(name.refine("xm_z_switch"), x_p, !z_x_is_neg * M))
        cxs.add(LP_LEQ(name.refine("xp_z_switch"), x_m, z_x_is_neg * M))

        return auxiliaries + cxs
    }
}

class IntAbs(name: SegName, x: LPAffExpr<Int>) : Abs<Int>(LPInteger(name), x)

class RealAbs(name: SegName, x: LPAffExpr<Double>) : Abs<Double>(LPReal(name), x)
