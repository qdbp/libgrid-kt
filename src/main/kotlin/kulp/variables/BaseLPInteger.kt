package kulp.variables

import kulp.LPAffExpr
import kulp.LPNode
import kulp.NodeBoundRenderable
import kulp.domains.LPDomain
import kulp.domains.LPIntegralDomain
import kulp.expressions.IntAffExpr

// base class for variable-like integers
abstract class BaseLPInteger(node: LPNode) :
    LPVar<Int>, LPAffExpr<Int> by IntAffExpr(mapOf(node.path to 1), 0), NodeBoundRenderable(node) {
    final override val dom: LPDomain<Int> = LPIntegralDomain
}
