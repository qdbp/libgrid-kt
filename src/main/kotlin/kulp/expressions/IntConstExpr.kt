package kulp.expressions

import ivory.interval.ClosedInterval
import kulp.LPBounded
import kulp.LPBoundedExpr
import kulp.LPNode
import kulp.LPPath

class IntConstExpr(override val constant: Int) : BaseLPIntExpr(), LPBoundedExpr<Int> {
    override val terms: Map<LPPath, Int> = mapOf()

    override fun resolve_bounds(root: LPNode): ClosedInterval<Int> = bounds

    override val bounds: ClosedInterval<Int> = ClosedInterval(constant)
}
