package kulp.transforms

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kulp.LPRenderable
import kulp.LPTransform
import kulp.MipContext
import kulp.aggregates.LPOneOfN
import kulp.constraints.LP_GEQ
import kulp.constraints.LP_LEQ
import kulp.times
import kulp.variables.LPInteger
import mdspan.NDSpan
import model.SegName
import nullable_fold

private fun make_output_var(name: SegName, vars: NDSpan<LPInteger>, which: String): LPInteger {
    return LPInteger(
        name.refine(which),
        vars.map { it.lb }.nullable_fold(::max),
        vars.map { it.ub }.nullable_fold(::min)
    )
}

/**
 * Returns a variable always equal to the maximum of the input variables.
 *
 * Auxiliaries: |vars| 1-of-N binary set Outputs: 1 Integer output variable
 */
class IntMax private constructor(val y: LPInteger, val vars: NDSpan<LPInteger>) :
    LPTransform<Int>(y) {

    constructor(
        name: SegName,
        vars: NDSpan<LPInteger>
    ) : this(make_output_var(name, vars, "max"), vars)

    constructor(name: SegName, vars: List<LPInteger>) : this(name, NDSpan(vars))

    private val selector = LPOneOfN(name.refine("bind_sel"), vars.shape)

    override fun render_auxiliaries(ctx: MipContext): List<LPRenderable> {
        val renderables: MutableList<LPRenderable> = mutableListOf(selector)
        // standard max formulation:
        // for all i: y >= x_i
        // exists j : y <= x_j
        // selector picks out j
        for (ix in vars.indices) {
            renderables.add(LP_GEQ(name.refine("max_gt").refine(ix), y, vars[ix]))
            renderables.add(
                LP_LEQ(
                    name.refine("max_le").refine(ix),
                    y,
                    vars[ix] + ctx.bigM.roundToInt() * !selector[ix]
                )
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
class IntMin private constructor(val y: LPInteger, val vars: NDSpan<LPInteger>) :
    LPTransform<Int>(y) {

    constructor(
        name: SegName,
        vars: NDSpan<LPInteger>
    ) : this(make_output_var(name, vars, "min"), vars)

    constructor(name: SegName, vars: List<LPInteger>) : this(name, NDSpan(vars))

    private val selector = LPOneOfN(name.refine("bind_sel"), vars.shape)

    override fun render_auxiliaries(ctx: MipContext): List<LPRenderable> {
        // standard min formulation:
        // for all i: y <= x_i
        // exists j : y >= x_j
        // selector picks out j
        val renderables: MutableList<LPRenderable> = mutableListOf(selector)
        for (ix in vars.indices) {
            renderables.add(LP_LEQ(name.refine("le_input").refine(ix), y, vars[ix]))
            renderables.add(
                LP_GEQ(
                    name.refine("ge_bind").refine(ix),
                    y,
                    vars[ix] - ctx.bigM.roundToInt() * !selector[ix]
                )
            )
        }
        return renderables
    }
}
