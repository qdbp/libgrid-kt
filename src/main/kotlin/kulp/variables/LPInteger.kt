package kulp.variables

import kulp.LPDomain
import kulp.LPName
import kulp.constraints.LPConstraint

class LPInteger(name: LPName) : LPVariable(name, LPDomain.Integral) {
    override fun intrinsic_constraints(): List<LPConstraint> = listOf()
}
