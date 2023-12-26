package kulp.variables

import kulp.*
import kulp.domains.LPDomain
import kulp.domains.LPIntegralDomain
import kulp.expressions.IntAffExpr

// base class for variable-like integers
context(BindCtx)
abstract class BaseLPInteger :
    LPVar<Int>,
    LPAffExpr<Int> by IntAffExpr(mapOf(unsafe_path_of_new_node to 1), 0),
    NodeBoundRenderable() {
    final override val dom: LPDomain<Int> = LPIntegralDomain
}
