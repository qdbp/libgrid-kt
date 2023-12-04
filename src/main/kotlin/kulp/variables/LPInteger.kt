package kulp.variables

import kulp.*
import model.SegName

open class LPInteger(
    override val name: SegName,
    override val lb: Int? = null,
    override val ub: Int? = null,
) : PrimitiveLPVariable<Int>(), LPAffExpr<Int> by IntAffExpr(mapOf(name to 1), 0) {
    override val domain: LPDomain = LPDomain.Integral
}
