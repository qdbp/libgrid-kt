package kulp.variables

import ivory.interval.ClosedInterval
import kulp.BindCtx
import kulp.LPNode
import kulp.NodeCtx

/** Base of the sealed primitive hierarchy for reals */
sealed class PrimitiveLPReal(node: LPNode, lb: Double? = null, ub: Double? = null) : BaseLPReal(node) {

    override val bounds = ClosedInterval(lb, ub)

    context(NodeCtx)
    override fun reify(): LPVar<Double> = this
}
