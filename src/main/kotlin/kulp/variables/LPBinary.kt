package kulp.variables

import ivory.interval.ClosedInterval
import kulp.BindCtx
import kulp.LPAffExpr
import kulp.LPNode
import kulp.expressions.PosBinary
import kulp.minus

class LPBinary private constructor(node: LPNode) : PrimitiveLPInteger(node, 0, 1) {
    companion object {
        context(BindCtx)
        operator fun invoke(): LPBinary = LPBinary(take())
    }

    operator fun not(): LPAffExpr<Int> = 1 - this

    override val bounds: ClosedInterval<Int> = ClosedInterval(0, 1)

    fun lift01(): PosBinary = PosBinary(this)
}
