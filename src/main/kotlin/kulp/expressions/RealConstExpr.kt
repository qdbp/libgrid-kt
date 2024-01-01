package kulp.expressions

import ivory.interval.ClosedInterval
import kulp.LPAffExpr
import kulp.LPBounded
import kulp.LPNode
import kulp.LPPath

data class RealConstExpr(override val constant: Double) :
    BaseLPRealExpr(), LPAffExpr<Double>, LPBounded<Double> {
    override val terms: Map<LPPath, Double> = mapOf()

    override fun compute_bounds(root: LPNode): ClosedInterval<Double> = bounds

    override val bounds: ClosedInterval<Double> = ClosedInterval(constant)
}
