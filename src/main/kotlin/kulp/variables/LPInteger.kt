package kulp.variables

import kulp.IntAffExpr
import kulp.LPAffExpr
import kulp.LPDomain
import model.SegName

open class LPInteger(
    override val name: SegName,
    val lb: Int? = null,
    val ub: Int? = null,
) : LPVariable<Int>, LPAffExpr<Int> by IntAffExpr(mapOf(name to 1), 0) {
    override val domain: LPDomain = LPDomain.Integral
}
