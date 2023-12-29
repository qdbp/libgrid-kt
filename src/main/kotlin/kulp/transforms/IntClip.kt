package kulp.transforms

import ivory.interval.ClosedInterval
import kulp.*
import kulp.expressions.gt
import kulp.variables.LPBinary
import kulp.variables.LPVar

/**
 * Returns a variable that is constrained to equal clip(expr, lb, ub) Cost:
 *
 * 1 integer output unless lb == 0 and ub == 1, in which case a binary output is used
 *
 * Costs:
 *
 * If both bounds are finite:
 * - 2 binary auxiliaries
 * - 8 constraints
 *
 * If only one bound is finite:
 * - 1 binary auxiliary
 * - 5 constraints
 */
context(BindCtx)
class IntClip
private constructor(
    self: LPVar<Int>,
    val x: LPAffExpr<Int>,
    val clip_lb: Int?,
    val clip_ub: Int?,
) : LPTransform<Int>(self) {

    companion object {
        // workaround for KT-57183
        context(BindCtx)
        operator fun invoke(x: LPAffExpr<Int>, clip_lb: Int?, clip_ub: Int?): IntClip =
            IntClip(self_by_example(x, clip_lb, clip_ub), x, clip_lb, clip_ub)

        // clip is a very expensive operation, so we hide the constructor and instead provide
        // optimized specialization for common cases
        context(BindCtx)
        @Suppress("UNCHECKED_CAST")
        fun clip(x: LPAffExpr<Int>, clip_lb: Int?, clip_ub: Int?): LPVar<Int> {
            val base_bounds: ClosedInterval<Int> =
                when (x) {
                    is LPBounded<*> -> x.bounds
                    else -> x.resolve_bounds(root)
                }
                    as ClosedInterval<Int>

            // if our clip lower bound <= the proven inherent lower bound, we can skip it
            // if, on the other hand, it is greater than the proven upper bound, it's infeasible
            val resolved_clip_lb =
                clip_lb?.let { clb ->
                    base_bounds.check_in_bounds(clb).let {
                        when (it) {
                            ClosedInterval.IvalRel.LLB -> null
                            ClosedInterval.IvalRel.Contains ->
                                // need this annoying caveat to also elide when we're on the edge
                                // of a finite interval without ruling that case infeasible.
                                if (clb > (base_bounds.lb ?: Int.MIN_VALUE)) clb else null
                            ClosedInterval.IvalRel.GUB -> throw ProvenInfeasible()
                        }
                    }
                }
            // likewise for the upper bound
            val resolved_clip_ub =
                clip_ub?.let { cub ->
                    base_bounds.check_in_bounds(cub).let {
                        when (it) {
                            ClosedInterval.IvalRel.LLB -> throw ProvenInfeasible()
                            ClosedInterval.IvalRel.Contains ->
                                if (cub < (base_bounds.ub ?: Int.MAX_VALUE)) cub else null
                            ClosedInterval.IvalRel.GUB -> null
                        }
                    }
                }
            return when {
                resolved_clip_lb == null && resolved_clip_ub == null -> x.reify()
                else -> IntClip(x, resolved_clip_lb, resolved_clip_ub)
            }
        }
    }

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
    val z_lb = clip_lb?.let { "z_lb"(::LPBinary) }
    val z_ub = clip_ub?.let { "z_ub"(::LPBinary) }

    context(NodeCtx)
    override fun decompose(ctx: LPContext) {
        require(ctx is BigMCapability)
        if (z_ub != null) {
            require(clip_ub != null)
            // enforce that the binding variables == 1 iff x > ub
            // == 1 if x >= ub:  Mz > x - ub
            "z_ub_bind_clipped" { (ctx.intM * z_ub) gt (x - clip_ub) }
            // == 0 if x < ub: M(1 - z) >= ub - x
            "z_ub_bind_free" { (ctx.intM * !z_ub) ge (clip_ub - x) }
            // enforce that y == ub iff z == 1, else y >= x
            // 1. y >= x - Mz
            "y_ge_x_midrange" { y ge (x - ctx.intM * z_ub) }
            // 2. y >= ub - M(1 - z)
            "y_ge_ub_clipped" { y ge (clip_ub - ctx.intM * !z_ub) }
            // if we have no lower bound, we also need to constrain that y never exceeds x
            // (this is handled by the lower bound logic otherwise)
            if (z_lb == null) "y_le_x" { y le x }
        }
        // this is by analogy to the above
        if (z_lb != null) {
            require(clip_lb != null)
            // == 1 if x <= lb: Mz > lb - x
            "z_lb_bind_clipped" { ctx.intM * z_lb gt clip_lb - x }
            // == 0 if x > lb: M(1 - z) >= x - lb
            "z_lb_bind_free" { ctx.intM * !z_lb ge x - clip_lb }
            // enforce that y == lb iff z == 1, else y <= x
            // 1. y <= x + Mz
            "y_le_x_midrange" { y le x + ctx.intM * z_lb }
            // 2. y <= lb + M(1 - z)
            "y_le_lb_clipped" { y le clip_lb + ctx.intM * !z_lb }
            // similarly, if we have no upper bound, we need to constrain that y never goes below x
            if (z_ub == null) "y_ge_x" { y ge x }
        }
    }
}
