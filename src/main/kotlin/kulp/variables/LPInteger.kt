package kulp.variables

import kulp.LPDomain
import kulp.constraints.LPConstraint
import kulp.constraints.LP_LEQ
import model.SegName

open class LPInteger(
    name: SegName,
    val lb: Int? = null,
    val ub: Int? = null,
) : LPVariable(name, LPDomain.Integral) {
    override fun intrinsic_constraints(): List<LPConstraint> {
        val out = mutableListOf<LPConstraint>()
        lb?.let { out.add(LP_LEQ(name.refine("lb"), it, this.as_expr())) }
        ub?.let { out.add(LP_LEQ(name.refine("ub"), this.as_expr(), it)) }
        return out
    }
}
