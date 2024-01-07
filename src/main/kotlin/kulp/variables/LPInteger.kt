package kulp.variables

import kulp.BindCtx
import kulp.LPNode

/** A primitive LP integer without bounds */
class LPInteger private constructor(node: LPNode, lb: Int?, ub: Int?) :
    PrimitiveLPInteger(node, lb, ub) {
    companion object {
        context(BindCtx)
        operator fun invoke(lb: Int? = null, ub: Int? = null): LPInteger = LPInteger(take(), lb, ub)
    }
}
