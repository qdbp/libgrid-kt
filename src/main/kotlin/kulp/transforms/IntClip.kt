package kulp.transforms

import kulp.*
import kulp.variables.LPBinary
import kulp.variables.LPVar

/**
 * Returns a variable that is constrained to equal clip(expr, lb, ub) Cost:
 *
 * 1 integer output unless lb == 0 and ub == 1, in which case a binary output is used
 *
 * If both bounds are finite:
 * - 2 binary auxiliaries
 * - 8 constraints If only one bound is finite:
 * - 1 binary auxiliary
 * - 4 constraints
 */
class IntClip(node: LPNode, val x: LPAffExpr<Int>, val clip_lb: Int?, val clip_ub: Int?) :
    LPTransform<Int>(node, Integral) {

    override val lb = clip_lb
    override val ub = clip_ub

    init {
        // forbid trivial cases such as == or +-1
        if (clip_lb != null && clip_ub != null) {
            require(clip_lb < clip_ub)
        }
        // require that at least one bound is finite, otherwise this is useless
        require(clip_lb != null || clip_ub != null)
    }

    // we make our binding variables public for testing and for conditioning on
    // note: we choose the convention that we are bound at x == ub/lb
    // this way z == 1 <=> x == bound, and this can be used as an implicit test for equality
    val z_lb = clip_lb?.let { node grow { LPBinary(it) } named "z_lb" }
    val z_ub = clip_ub?.let { node grow { LPBinary(it) } named "z_ub" }

    override fun decompose_auxiliaries(node: LPNode, out: LPVar<Int>, ctx: LPContext) {
        require(ctx is BigMCapability)

        if (z_ub != null) {
            require(clip_ub != null)
            node +=
                listOf(
                    // enforce that the binding variables == 1 iff x > ub
                    // == 1 if x >= ub:  Mz > x - ub
                    (ctx.intM * z_ub) gt (x - clip_ub) named "z_ub_bind_clipped",
                    // == 0 if x < ub: M(1 - z) >= ub - x
                    (ctx.intM * !z_ub) ge (clip_ub - x) named "z_ub_bind_free",
                    // enforce that y == ub iff z == 1, else y >= x
                    // 1. y >= x - Mz
                    out ge (x - ctx.intM * z_ub) named "y_ge_x_midrange",
                    // 2. y >= ub - M(1 - z)
                    out ge (clip_ub - ctx.intM * !z_ub) named "y_ge_ub_clipped",
                )
            // if we have no lower bound, we also need to constrain that y never exceeds x
            // (this is handled by the lower bound logic otherwise)
            if (z_lb == null) node += out le x named "y_le_x"
        }
        // this is by analogy to the above
        if (z_lb != null) {
            require(clip_lb != null)
            node +=
                listOf(
                    // == 1 if x <= lb: Mz > lb - x
                    ctx.intM * z_lb gt clip_lb - x named "z_lb_bind_clipped",
                    // == 0 if x > lb: M(1 - z) >= x - lb
                    ctx.intM * !z_lb ge x - clip_lb named "z_lb_bind_free",
                    // enforce that y == lb iff z == 1, else y <= x
                    // 1. y <= x + Mz
                    out le x + ctx.intM * z_lb named "y_le_x_midrange",
                    // 2. y <= lb + M(1 - z)
                    out le clip_lb + ctx.intM * !z_lb named "y_le_lb_clipped",
                )
            // similarly, if we have no upper bound, we need to constrain that y never goes below x
            if (z_ub == null) node += out ge x named "y_ge_x"
        }
    }
}
