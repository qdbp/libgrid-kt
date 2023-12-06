package kulp.transforms

import kulp.*
import kulp.variables.LPBinary
import kulp.variables.LPInteger
import model.LPName

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
// TODO make generic
class IntClip
private constructor(val y: LPInteger, val x: LPAffExpr<Int>, val clip_lb: Int?, val clip_ub: Int?) :
    LPTransform<Int>(y) {

    companion object {
        private fun make_output_var(name: LPName, lb: Int?, ub: Int?): LPInteger {
            return LPInteger(name.refine("y"), lb, ub)
        }
    }

    constructor(x: LPInteger, lb: Int?, ub: Int?) : this(make_output_var(x.name, lb, ub), x, lb, ub)

    constructor(
        name: LPName,
        x: LPAffExpr<Int>,
        lb: Int?,
        ub: Int?
    ) : this(make_output_var(name, lb, ub), x, lb, ub)

    val z_lb = clip_lb?.let { LPBinary(name.refine("z_lb")) }
    val z_ub = clip_ub?.let { LPBinary(name.refine("z_ub")) }

    init {
        // forbid trivial cases such as == or +-1
        if (clip_lb != null && clip_ub != null) {
            require(clip_lb < clip_ub - 1)
        }
        // require that at least one bound is finite, otherwise this is useless
        require(clip_lb != null || clip_ub != null)
    }

    private fun LPName.get_constraints_for_M(M: Int): List<LPRenderable> {
        // note: we choose the convention that we are bound at x == ub/lb
        // this way z == 1 <=> x == bound, and this can be used as an implicit test for equality
        val out = mutableListOf<LPConstraint>()
        if (z_ub != null) {
            require(clip_ub != null)
            out +=
                listOf(
                    // enforce that the binding variables == 1 iff x > ub
                    // == 1 if x >= ub:  Mz > x - ub
                    (M * z_ub) gt (x - clip_ub) named "z_ub_bind_clipped",
                    // == 0 if x < ub: M(1 - z) >= ub - x
                    (M * !z_ub) ge (clip_ub - x) named "z_ub_bind_free",
                    // enforce that y == ub iff z == 1, else y >= x
                    // 1. y >= x - Mz
                    y ge (x - M * z_ub) named "y_ge_x_midrange",
                    // 2. y >= ub - M(1 - z)
                    y ge (clip_ub - M * !z_ub) named "y_ge_ub_clipped",
                )
            // if we have no lower bound, we also need to constrain that y never exceeds x
            // (this is handled by the lower bound logic otherwise)
            if (z_lb == null) out += y le x named "y_le_x"
        }
        // this is by analogy to the above
        if (z_lb != null) {
            require(clip_lb != null)
            out +=
                listOf(
                    // == 1 if x <= lb: Mz > lb - x
                    M * z_lb gt clip_lb - x named "z_lb_bind_clipped",
                    // == 0 if x > lb: M(1 - z) >= x - lb
                    M * !z_lb ge x - clip_lb named "z_lb_bind_free",
                    // enforce that y == lb iff z == 1, else y <= x
                    // 1. y <= x + Mz
                    y le x + M * z_lb named "y_le_x_midrange",
                    // 2. y <= lb + M(1 - z)
                    y le clip_lb + M * !z_lb named "y_le_lb_clipped",
                )
            // similarly, if we have no upper bound, we need to constrain that y never goes below x
            if (z_ub == null) out += y ge x named "y_ge_x"
        }
        return out
    }

    override fun LPName.render_auxiliaries(ctx: LPContext): List<LPRenderable> {
        require(ctx is BigMCapability)
        return with(name) { get_constraints_for_M(ctx.intM) } + listOfNotNull(z_lb, z_ub)
    }
}
