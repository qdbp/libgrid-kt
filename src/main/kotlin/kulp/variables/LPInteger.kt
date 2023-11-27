package kulp.variables

import kulp.LPDomain
import kulp.constraints.LPConstraint
import kulp.constraints.LP_LEQ
import model.SegName

open class LPInteger(
    name: SegName,
    private val lb: ILPIntBound = LPInfinite,
    private val ub: ILPIntBound = LPInfinite
) : LPVariable(name, LPDomain.Integral) {
    override fun intrinsic_constraints(): List<LPConstraint> {
        val out = mutableListOf<LPConstraint>()
        when (lb) {
            is LPIntBound -> out.add(LP_LEQ(name.refine("lb"), lb.value, this.as_expr()))
            is LPInfinite -> {}
        }
        when (ub) {
            is LPIntBound -> out.add(LP_LEQ(name.refine("ub"), this.as_expr(), ub.value))
            is LPInfinite -> {}
        }
        return out
    }
}
