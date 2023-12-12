package kulp.variables

import kulp.BindCtx
import kulp.LPAffExpr
import kulp.LPDomain
import kulp.NodeBoundRenderable
import kulp.domains.Real
import kulp.expressions.RealAffExpr

context(BindCtx)
abstract class BaseLPReal :
    LPVar<Double>,
    LPAffExpr<Double> by RealAffExpr(mapOf(unsafe_path_of_new_node to 1.0), 0.0),
    NodeBoundRenderable() {
    final override val dom: LPDomain<Double> = Real
}
