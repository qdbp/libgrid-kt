package kulp.transforms

import kulp.*
import kulp.variables.LPBinary
import kulp.variables.LPVar

class Abs<N : Number>(node: LPNode, val x: LPAffExpr<N>) : LPTransform<N>(node, x.domain) {

    override val lb = domain.zero
    override val ub = null

    override fun decompose_auxiliaries(node: LPNode, out: LPVar<N>, ctx: LPContext) {
        require(ctx is BigMCapability)
        // always need the int M here
        val M = ctx.intM

        val x_p = node grow { domain.newvar(it) } named "pos"
        val x_n = node grow { domain.newvar(it) } named "neg"

        val z_x_is_neg = node grow { LPBinary(it) } named "z_x_is_neg"

        node +=
            listOf(
                x_p.gez named "xp_ge_0",
                x_n.gez named "xm_ge_0",
                x eq x_p - x_n named "xp_minus_xm_is_x",
                out eq x_p + x_n named "out_bind",
                x_p le (!z_x_is_neg * M) named "xp_z_switch",
                x_n le (z_x_is_neg * M) named "xm_z_switch",
            )
    }
}
