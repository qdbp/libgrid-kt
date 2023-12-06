package kulp.transforms

import kulp.*
import kulp.variables.LPBinary
import kulp.variables.LPInteger
import kulp.variables.LPReal
import kulp.variables.PrimitiveLPVariable
import model.LPName

sealed class Abs<N : Number>(y: PrimitiveLPVariable<N>, val x: LPAffExpr<N>) : LPTransform<N>(y) {

    /** We build constraints dynamically, since we need bigM to be known. */
    override fun LPName.render_auxiliaries(ctx: LPContext): List<LPRenderable> {
        require(ctx is BigMCapability)
        // always need the int M here
        val M = ctx.intM

        // we can build our auxiliaries statically
        // override val name: SegName = x.name.refine(transform_identifier())
        val auxiliaries: MutableList<LPVariable<N>> = mutableListOf()
        val x_p = new_self_type_lpvar(+"p")
        val x_m = new_self_type_lpvar(+"m")

        // z == 1 <=> x <= 0
        val z_x_is_neg = LPBinary(+"is_negative")
        auxiliaries.add(x_p)
        auxiliaries.add(x_m)

        val cxs =
            listOf(
                x_p.gez() named "xp_ge_0",
                x_m.gez() named "xm_ge_0",
                x eq x_p - x_m named "xp_minus_xm_is_x",
                output eq x_m + x_p named "abs_bind",
                x_p le (!z_x_is_neg * M) named "xp_z_switch",
                x_m le (z_x_is_neg * M) named "xm_z_switch",
            )

        return auxiliaries + cxs
    }
}

class IntAbs(name: LPName, x: LPAffExpr<Int>) : Abs<Int>(LPInteger(name), x)

class RealAbs(name: LPName, x: LPAffExpr<Double>) : Abs<Double>(LPReal(name), x)
