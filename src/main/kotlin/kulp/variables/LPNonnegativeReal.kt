package kulp.variables

import kulp.constraints.LPConstraint
import kulp.constraints.LPLEQ

class LPNonnegativeReal(name: String): LPReal(name) {

    override fun intrinsic_constraints(): List<LPConstraint> = listOf(
        LPLEQ(
            "${intrinsic_prefix()}_lb", 0, this.as_expr()
        ),
    )
}
