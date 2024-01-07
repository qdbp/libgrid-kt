package kulp.variables

import kulp.LPAffExpr
import kulp.LPNode
import kulp.NodeBoundRenderable
import kulp.domains.LPDomain
import kulp.domains.LPRealDomain
import kulp.expressions.RealAffExpr

abstract class BaseLPReal(node: LPNode) :
    LPVar<Double>,
    LPAffExpr<Double> by RealAffExpr(mapOf(node.path to 1.0), 0.0),
    NodeBoundRenderable(node) {
    final override val dom: LPDomain<Double> = LPRealDomain
}
