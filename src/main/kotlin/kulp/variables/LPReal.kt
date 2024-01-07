package kulp.variables

import kulp.BindCtx
import kulp.LPNode

/** Unbounded primitive LP real. */
class LPReal(node: LPNode, lb: Double?, ub: Double?) : PrimitiveLPReal(node, lb, ub) {
    companion object {
        context(BindCtx)
        operator fun invoke(lb: Double? = null, ub: Double? = null): LPReal = LPReal(take(), lb, ub)
    }
}
