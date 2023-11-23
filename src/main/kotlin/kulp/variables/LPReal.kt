package kulp.variables

import kulp.LPDomain
import kulp.constraints.LPConstraint

open class LPReal(name: String) : LPVariable(name, LPDomain.Real) {
    override fun intrinsic_constraints(): List<LPConstraint> = listOf()
}
