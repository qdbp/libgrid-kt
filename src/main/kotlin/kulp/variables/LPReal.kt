package kulp.variables

import kulp.LPAffExpr
import kulp.LPDomain
import kulp.RealAffExpr
import model.LPName

open class LPReal(
    override val name: LPName,
    override val lb: Double? = null,
    override val ub: Double? = null
) : PrimitiveLPVariable<Double>(), LPAffExpr<Double> by RealAffExpr(mapOf(name to 1.0), 0.0) {
    override val domain: LPDomain = LPDomain.Real
}
