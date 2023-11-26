package kulp.variables

import kulp.LPDomain
import kulp.LPName
import kulp.constraints.LPConstraint
import kulp.constraints.LP_LEQ

class LPNonnegativeInteger(name: LPName) : LPVariable(name, LPDomain.Integral) {
    override fun intrinsic_constraints(): List<LPConstraint> = listOf(
        LP_LEQ(name.refine("lb"), 0, this.as_expr())
    )
}
