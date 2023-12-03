package kulp.transforms

import kotlin.math.roundToInt
import kulp.*
import kulp.constraints.LPConstraint
import kulp.constraints.LP_GEQ
import kulp.constraints.LP_LEQ
import kulp.variables.LPBinary
import kulp.variables.LPInteger

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
class IntClip
private constructor(val y: LPInteger, val x: LPInteger, val clip_lb: Int?, val clip_ub: Int?) :
    LPTransform<Int>(y), LPAffExpr<Int> {

    companion object {
        private fun make_output_var(x: LPInteger, lb: Int?, ub: Int?): LPInteger {
            return LPInteger(x.name.refine("y"), lb, ub)
        }
    }

    constructor(x: LPInteger, lb: Int?, ub: Int?) : this(make_output_var(x, lb, ub), x, lb, ub)

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

    private fun get_constraints_for_M(bigM: Double): List<LPRenderable> {

        // note: we choose the convention that we are bound at x == ub/lb
        // this way z == 1 <=> x == bound, and this can be used as an implicit test for equality
        val M = bigM.roundToInt()

        val out = mutableListOf<LPConstraint>()
        if (z_ub != null) {
            require(clip_ub != null)
            out +=
                listOf(
                    // enforce that the binding variables == 1 iff x > ub
                    // == 1 if x >= ub:  Mz > x - ub
                    LP_GEQ(name.refine("z_ub_bind_gt"), M * z_ub, x - clip_ub + 1),
                    // == 0 if x < ub: M(1 - z) >= ub - x
                    LP_GEQ(name.refine("z_ub_bind_le"), M * !z_ub, clip_ub - x),
                    // enforce that y == ub iff z == 1, else y >= x
                    // 1. y >= x - Mz
                    LP_GEQ(name.refine("y_ge_x_midrange"), y, x - M * z_ub),
                    // 2. y >= ub - M(1 - z)
                    LP_GEQ(name.refine("y_ge_ub_clipped"), y, -M * !z_ub + clip_ub),
                )
            // if we have no lower bound, we also need to constrain that y never exceeds x
            // (this is handled by the lower bound logic otherwise)
            if (z_lb == null) {
                out.add(LP_LEQ(name.refine("y_le_x"), y, x))
            }
        }
        // this is by analogy to the above
        if (z_lb != null) {
            require(clip_lb != null)
            out +=
                listOf(
                    // == 1 if x <= lb: Mz > lb - x
                    LP_GEQ(name.refine("z_lb_bind_lt"), M * z_lb, clip_lb - x + 1),
                    // == 0 if x > lb: M(1 - z) >= x - lb
                    LP_GEQ(name.refine("z_lb_bind_ge"), M * !z_lb, x - clip_lb),
                    // enforce that y == lb iff z == 1, else y <= x
                    // 1. y <= x + Mz
                    LP_LEQ(name.refine("y_le_x_midrange"), y, x + M * z_lb),
                    // 2. y <= lb + M(1 - z)
                    LP_LEQ(name.refine("y_le_lb_clipped"), y, M * !z_lb + clip_lb),
                )
            // similarly, if we have no upper bound, we need to constrain that y never goes below x
            if (z_ub == null) {
                out.add(LP_GEQ(name.refine("y_ge_x"), y, x))
            }
        }
        return out
    }

    override fun is_primitive(ctx: MipContext): Boolean = false

    override fun render(ctx: MipContext): List<LPRenderable> {
        val constraints: List<LPRenderable> = get_constraints_for_M(ctx.bigM)
        return constraints + listOfNotNull(y, z_lb, z_ub)
    }
}

fun BoolClip(x: LPBinary) = IntClip(x, 0, 1)
