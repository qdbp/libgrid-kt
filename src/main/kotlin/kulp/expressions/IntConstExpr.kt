package kulp.expressions

import ivory.interval.ClosedInterval
import kulp.LPAffExpr
import kulp.LPBounded
import kulp.LPNode
import kulp.LPPath

open class IntConstExpr(final override val constant: Int) :
    BaseLPIntExpr(), LPAffExpr<Int>, LPBounded<Int> {
    final override val terms: Map<LPPath, Int> = mapOf()

    final override fun compute_bounds(root: LPNode): ClosedInterval<Int> = bounds

    override val bounds: ClosedInterval<Int> = ClosedInterval(constant)

    companion object {
        val Int.lp: LPAffExpr<Int>
            get() =
                when {
                    this == 0 -> Zero
                    this == 1 -> One
                    else -> IntConstExpr(this)
                }
    }
}
