package kulp.variables

import ivory.interval.ClosedInterval
import kulp.BindCtx
import kulp.LPNode
import kulp.NodeCtx

/** A primitive LP integer */
/** Base of the sealed primitive hierarchy for reals */
sealed class PrimitiveLPInteger(node: LPNode, lb: Int? = null, ub: Int? = null) : BaseLPInteger(node) {

    override val bounds = ClosedInterval(lb, ub)

    context(NodeCtx)
    override fun reify(): LPVar<Int> = this
}
