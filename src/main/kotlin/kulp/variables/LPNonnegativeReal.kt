package kulp.variables

import model.SegName
import kulp.constraints.LPConstraint
import kulp.constraints.LP_LEQ

class LPNonnegativeReal(name: SegName): LPReal(name) {

    override fun intrinsic_constraints(): List<LPConstraint> = listOf(
        LP_LEQ(name.refine("lb"), 0, this.as_expr())
    )
}
