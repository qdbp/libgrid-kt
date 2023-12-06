package kulp.transforms

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kulp.*
import kulp.aggregates.LPOneOfN
import kulp.variables.LPInteger
import mdspan.NDSpan
import model.LPName
import nullable_fold

/**
 * Private base class for IntMin and IntMax.
 */
sealed class IntMinMax(name: LPName, val vars: NDSpan<LPInteger>, private val which: Which) :
    LPTransform<Int>(make_output_var(name, vars, which)) {

    companion object {
        private fun make_output_var(
            name: LPName,
            vars: NDSpan<LPInteger>,
            which: Which
        ): LPInteger {
            return LPInteger(
                name.refine(which.name),
                vars.map { it.lb }.nullable_fold(::max),
                vars.map { it.ub }.nullable_fold(::min)
            )
        }
    }

    protected enum class Which {
        min,
        max
    }

    override fun LPName.render_auxiliaries(ctx: LPContext): List<LPRenderable> {
        require(ctx is BigMCapability)
        val M = ctx.bigM.roundToInt()

        val selector = LPOneOfN(name.refine("bind_sel"), vars.shape)
        val renderables: MutableList<LPRenderable> = mutableListOf(selector)

        // standard max formulation:
        // for all i: y >= x_i
        // exists j : y <= x_j
        // selector picks out j
        for (ix in vars.indices) {
            with(+ix) {
                when (which) {
                    Which.max -> {
                        renderables += output ge vars[ix] named "ge_bind"
                        renderables += output le vars[ix] + M * !selector[ix] named "le_bind"
                    }
                    Which.min -> {
                        renderables += output le vars[ix] named "le_bind"
                        renderables += output ge vars[ix] - M * !selector[ix] named "ge_bind"
                    }
                }
            }
        }
        return renderables
    }
}

/**
 * Returns a variable always equal to the minimum of the input variables.
 *
 * Auxiliaries: |vars| 1-of-N binary set Outputs: 1 Integer output variable
 */
class IntMin(name: LPName, vars: NDSpan<LPInteger>) : IntMinMax(name, vars, Which.min) {
    constructor(name: LPName, vars: List<LPInteger>) : this(name, NDSpan(vars))
}

/**
 * Returns a variable always equal to the maximum of the input variables.
 *
 * Auxiliaries: |vars| 1-of-N binary set Outputs: 1 Integer output variable
 */
class IntMax(name: LPName, vars: NDSpan<LPInteger>) : IntMinMax(name, vars, Which.max) {
    constructor(name: LPName, vars: List<LPInteger>) : this(name, NDSpan(vars))
}
