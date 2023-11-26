package kulp.variables

import kulp.LPDomain
import kulp.LPName
import kulp.constraints.LPConstraint
import kulp.constraints.LP_LEQ

class LPBinary(name: LPName) : LPVariable(name, LPDomain.Integral) {
    override fun intrinsic_constraints(): List<LPConstraint> {
        return listOf(
            LP_LEQ(name.refine("lb"), 0, this.as_expr()),
            LP_LEQ(name.refine("ub"), this.as_expr(), 1),
        )
    }

}
