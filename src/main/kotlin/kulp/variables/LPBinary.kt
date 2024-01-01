package kulp.variables

import ivory.interval.ClosedInterval
import kulp.BindCtx
import kulp.LPAffExpr
import kulp.expressions.PosBinary
import kulp.minus

context(BindCtx)
class LPBinary : PrimitiveLPInteger(0, 1) {
    operator fun not(): LPAffExpr<Int> = 1 - this

    override val bounds: ClosedInterval<Int> = ClosedInterval(0, 1)

    fun lift01(): PosBinary = PosBinary(this)
}
