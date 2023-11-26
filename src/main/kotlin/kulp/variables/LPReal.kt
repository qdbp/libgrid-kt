package kulp.variables

import kulp.LPDomain
import kulp.LPName
import kulp.constraints.LPConstraint

open class LPReal(name: LPName) : LPVariable(name, LPDomain.Real) {
    override fun intrinsic_constraints(): List<LPConstraint> = listOf()
}
