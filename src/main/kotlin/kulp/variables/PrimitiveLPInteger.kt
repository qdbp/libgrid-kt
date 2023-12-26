package kulp.variables

import ivory.interval.ClosedInterval
import kulp.BindCtx

/** A primitive LP integer */
/** Base of the sealed primitive hierarchy for reals */
context(BindCtx)
sealed class PrimitiveLPInteger(lb: Int? = null, ub: Int? = null) : BaseLPInteger() {

    override val bounds = ClosedInterval(lb, ub)

    override fun reify(): LPVar<Int> = this
}
