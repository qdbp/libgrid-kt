package kulp.variables

import kulp.LPDomain
import kulp.constraints.LPConstraint
import kulp.constraints.LPLEQ

class LPBinary(name: String) : LPVariable(name, LPDomain.Integral) {
    override fun intrinsic_constraints(): List<LPConstraint> {
        return listOf(
            LPLEQ(
                "${intrinsic_prefix()}_lb", 0, this.as_expr()
            ),
            LPLEQ(
                "${intrinsic_prefix()}_ub", this.as_expr(), 1
            ),
        )
    }

}
