package kulp.variables

import kulp.LPDomain
import model.SegName
import kulp.constraints.LPConstraint

open class LPReal(name: SegName) : LPVariable(name, LPDomain.Real) {
    override fun intrinsic_constraints(): List<LPConstraint> = listOf()
}
