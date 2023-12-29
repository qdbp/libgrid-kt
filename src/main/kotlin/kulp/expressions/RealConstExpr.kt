package kulp.expressions

import ivory.interval.ClosedInterval
import kulp.LPBoundedExpr
import kulp.LPNode
import kulp.LPPath

data class RealConstExpr(override val constant: Double) : BaseLPRealExpr(), LPBoundedExpr<Double> {
    override val terms: Map<LPPath, Double> = mapOf()

    override fun resolve_bounds(root: LPNode): ClosedInterval<Double> = bounds

    override val bounds: ClosedInterval<Double> = ClosedInterval(constant)
}
