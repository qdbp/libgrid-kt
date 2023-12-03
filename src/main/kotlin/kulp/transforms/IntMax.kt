package kulp.transforms

import kotlin.math.max
import kotlin.math.min
import kulp.LPAffineExpression
import kulp.LPRenderable
import kulp.MipContext
import kulp.aggregates.LPOneOfN
import kulp.constraints.LP_GEQ
import kulp.constraints.LP_LEQ
import kulp.times
import kulp.variables.LPInteger
import mdspan.NDSpan
import model.SegName
import nullable_fold

/**
 * Returns a variable always equal to the maximum of the input variables.
 *
 * Auxiliaries: |vars| 1-of-N binary set Outputs: 1 Integer output variable
 */
class IntMax(override val name: SegName, val vars: NDSpan<LPInteger>) : LPTransform() {
    constructor(name: SegName, vars: List<LPInteger>) : this(name, NDSpan(vars))

    val y =
        LPInteger(
            name.refine("max"),
            vars.map { it.lb }.nullable_fold(::max),
            vars.map { it.ub }.nullable_fold(::min)
        )
    val selector = LPOneOfN(name.refine("bind_sel"), vars.shape)

    override fun as_expr(): LPAffineExpression = y.as_expr()

    override fun render(ctx: MipContext): List<LPRenderable> {
        val renderables = mutableListOf(selector, y)
        for (ix in vars.indices) {
            renderables.add(LP_GEQ(name.refine("max_gt").refine(ix), y, vars[ix]))
            renderables.add(
                LP_LEQ(name.refine("max_le").refine(ix), y, vars[ix] + ctx.bigM * !selector[ix])
            )
        }
        return renderables
    }
}

/**
 * Returns a variable always equal to the minimum of the input variables.
 *
 * Auxiliaries: |vars| 1-of-N binary set Outputs: 1 Integer output variable
 */
class IntMin(override val name: SegName, val vars: NDSpan<LPInteger>) : LPTransform() {
    constructor(name: SegName, vars: List<LPInteger>) : this(name, NDSpan(vars))

    val y =
        LPInteger(
            name.refine("min"),
            vars.map { it.lb }.nullable_fold(::max),
            vars.map { it.ub }.nullable_fold(::min)
        )
    val selector = LPOneOfN(name.refine("bind_sel"), vars.shape)

    override fun as_expr(): LPAffineExpression = y.as_expr()

    override fun render(ctx: MipContext): List<LPRenderable> {
        val renderables = mutableListOf(selector, y)
        for (ix in vars.indices) {
            renderables.add(LP_LEQ(name.refine("le_input").refine(ix), y, vars[ix]))
            renderables.add(
                LP_GEQ(name.refine("ge_bind").refine(ix), y, vars[ix] - ctx.bigM * !selector[ix])
            )
        }
        return renderables
    }
}
