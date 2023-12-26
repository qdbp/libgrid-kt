package kulp.variables

import ivory.interval.ClosedInterval
import kulp.BindCtx

/** Base of the sealed primitive hierarchy for reals */
context(BindCtx)
sealed class PrimitiveLPReal(lb: Double? = null, ub: Double? = null) : BaseLPReal() {

    override val bounds = ClosedInterval(lb, ub)

    override fun reify(): LPVar<Double> = this
}
