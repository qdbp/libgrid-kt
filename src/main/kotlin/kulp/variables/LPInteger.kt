package kulp.variables

import kulp.LPDomain
import kulp.constraints.LPConstraint

class LPInteger(name: String) : LPVariable(name, LPDomain.Integral) {
    override fun intrinsic_constraints(): List<LPConstraint> = listOf()
}
