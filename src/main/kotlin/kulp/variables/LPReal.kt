package kulp.variables

import kulp.LPAffExpr
import kulp.LPDomain
import kulp.RealAffExpr
import model.SegName

open class LPReal(override val name: SegName, val lb: Double? = null, val ub: Double? = null) :
    LPVariable<Double>, LPAffExpr<Double> by RealAffExpr(mapOf(name to 1.0), 0.0) {
    override val domain: LPDomain = LPDomain.Real
}
