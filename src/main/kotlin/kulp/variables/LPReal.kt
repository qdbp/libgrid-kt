package kulp.variables

import kulp.LPAffExpr
import kulp.LPDomain
import kulp.RealAffExpr
import model.SegName

open class LPReal(name: SegName, val lb: Double? = null, val ub: Double? = null) :
    LPVariable<Double>(name, LPDomain.Real),
    LPAffExpr<Double> by RealAffExpr(mapOf(name to 1.0), 0.0)
