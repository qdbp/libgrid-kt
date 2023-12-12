package kulp.transforms

// class Abs<N : Number> private constructor(self: LPVar<N>, val x: LPAffExpr<N>) :
//     LPTransform<N>(self) {
//
//     context(BindCtx)
//     companion object {
//         // workaround for KT-57183
//         operator fun <N : Number> invoke(x: LPAffExpr<N>): LPAffExpr<N> = Abs(self_by_example(x),
// x)
//     }
//
//     override fun LPNode.decompose(ctx: LPContext) {
//         require(ctx is BigMCapability)
//         // always need the int M here
//         val M = ctx.intM
//         val x_p = "pos" { dom.newvar(lb = dom.zero) }
//         val x_n = "neg" { dom.newvar(lb = dom.zero) }
//         val z_x_is_neg = "z_x_is_neg"(::LPBinary)
//         "xp_minus_xm_is_x" { x eq x_p - x_n }
//         "out_bind" { avatar eq x_p + x_n }
//         "xp_z_switch" { x_p le (!z_x_is_neg * M) }
//         "xm_z_switch" { x_n le (z_x_is_neg * M) }
//     }
// }
