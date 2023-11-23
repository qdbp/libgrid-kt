package kulp.variables

import kulp.LPDomain
import kulp.constraints.LPConstraint
import kulp.constraints.LPLEQ

class LPNonnegativeInteger(name: String) : LPVariable(name, LPDomain.Integral) {
    override fun intrinsic_constraints(): List<LPConstraint> = listOf(
        LPLEQ(
            "${intrinsic_prefix()}_lb", 0, this.as_expr()
        ),
    )
}
