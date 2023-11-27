package kulp.transforms

import kulp.*
import kulp.constraints.LPConstraint
import kulp.constraints.LP_GEQ
import kulp.constraints.LP_LEQ
import kulp.variables.*
import model.SegName

/** Returns a variable that is constrained to equal clip(expr, lb, ub) */
class IntClip(
    override val name: SegName,
    exprlike: LPExprLike,
    val lb: ILPIntBound,
    val ub: ILPIntBound,
) : Transform() {

    val z_lb: LPBinary? =
        when (lb) {
            is LPIntBound -> LPBinary(name.refine("z_lb_is_binding"))
            is LPInfinite -> null
        }
    val z_ub: LPBinary? =
        when (ub) {
            is LPIntBound -> LPBinary(name.refine("z_ub_is_binding"))
            is LPInfinite -> null
        }

    init {
        // forbid trivial cases such as == or +-1
        if (lb is LPIntBound && ub is LPIntBound) {
            require(lb.value < ub.value - 1)
        }
        // require that at least one bound is finite, otherwise this is useless
        require(lb is LPIntBound || ub is LPIntBound)
    }

    constructor(
        int: LPInteger,
        lb: ILPIntBound,
        ub: ILPIntBound
    ) : this(int.name.refine("clip[${lb}-$ub]"), int, lb, ub)

    val x: LPAffineExpression = exprlike.as_expr()
    val y = LPInteger(name.refine("out"), lb, ub)

    private fun get_constraints_for_M(M: Double): List<LPRenderable> {

        // note: we choose the convention that we are bound at x == ub/lb
        // this way z == 1 <=> x == bound, and this can be used as an implicit test for equality

        val out = mutableListOf<LPConstraint>()
        if (z_ub != null) {
            require(ub is LPIntBound)
            out +=
                listOf(
                    // enforce that the binding variables == 1 iff x > ub
                    // == 0 if x < ub: -M(1 - z) <= x - ub
                    LP_LEQ(name.refine("z_ub_bind_le"), -M * !z_ub, x - ub.value),
                    // == 1 if x >= ub:  Mz > x - ub
                    LP_GEQ(name.refine("z_ub_bind_gt"), M * z_ub, x - ub.value + 1),
                    // enforce that y == ub iff z == 1, else y >= x
                    // 1. y >= x - Mz
                    LP_GEQ(name.refine("y_ge_x_midrange"), y, x - M * z_ub),
                    // 2. y >= ub - M(1 - z)
                    LP_GEQ(name.refine("y_ge_ub_clipped"), y, -M * !z_ub + ub.value),
                )
            // if we have no lower bound, we also need to constrain that y never exceeds x
            // (this is handled by the lower bound logic otherwise)
            if (z_lb == null) {
                out.add(LP_LEQ(name.refine("y_le_x"), y, x))
            }
        }
        // this is by analogy to the above
        if (z_lb != null) {
            require(lb is LPIntBound)
            out +=
                listOf(
                    // == 0 if x >= lb: -M(1 - z) <= lb - x
                    LP_LEQ(name.refine("z_lb_bind_ge"), -M * !z_lb, lb.value - x),
                    // == 1 if x < lb: Mz > lb - x
                    LP_GEQ(name.refine("z_lb_bind_lt"), M * z_lb, lb.value - x + 1),
                    // enforce that y == lb iff z == 1, else y <= x
                    // 1. y <= x + Mz
                    LP_LEQ(name.refine("y_le_x_midrange"), y, x + M * z_lb),
                    // 2. y <= lb + M(1 - z)
                    LP_LEQ(name.refine("y_le_lb_clipped"), y, M * !z_lb + lb.value),
                )
            // similarly, if we have no upper bound, we need to constrain that y never goes below x
            if (z_ub == null) {
                out.add(LP_GEQ(name.refine("y_ge_x"), y, x))
            }
        }
        return out
    }

    override fun as_expr(): LPAffineExpression {
        return y.as_expr()
    }

    override fun is_primitive(ctx: MipContext): Boolean = false

    override fun render(ctx: MipContext): List<LPRenderable> {
        val constraints: List<LPRenderable> = get_constraints_for_M(ctx.bigM)
        return constraints + listOfNotNull(y, z_lb, z_ub)
    }
}

fun IntMax(int: LPInteger, bound: ILPIntBound): IntClip {
    return IntClip(int, bound, LPInfinite)
}

fun IntMax(name: SegName, expr: LPExprLike, bound: ILPIntBound): IntClip {
    return IntClip(name, expr, bound, LPInfinite)
}

fun IntMin(int: LPInteger, bound: ILPIntBound): IntClip {
    return IntClip(int, LPInfinite, bound)
}

fun IntMin(name: SegName, expr: LPExprLike, bound: ILPIntBound): IntClip {
    return IntClip(name, expr, LPInfinite, bound)
}
