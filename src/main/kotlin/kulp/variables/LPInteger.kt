package kulp.variables

import kulp.IntAffExpr
import kulp.LPAffExpr
import kulp.LPDomain
import model.SegName

open class LPInteger(
    name: SegName,
    val lb: Int? = null,
    val ub: Int? = null,
) : LPVariable<Int>(name, LPDomain.Integral), LPAffExpr<Int> by IntAffExpr(mapOf(name to 1), 0)
