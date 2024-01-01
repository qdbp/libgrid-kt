package kulp.domains

import ivory.algebra.DoubleRing
import ivory.order.DoubleOrder
import kulp.BindCtx
import kulp.LPAffExpr
import kulp.LPPath
import kulp.expressions.RealAffExpr
import kulp.variables.LPReal
import kulp.variables.LPVar

data object LPRealDomain :
    LPDomain<Double>(
        klass = Double::class,
        ring = DoubleRing,
        order = DoubleOrder,
    ) {
    context(BindCtx)
    override fun newvar(lb: Double?, ub: Double?): LPVar<Double> = LPReal(lb, ub)

    override fun newexpr(terms: Map<LPPath, Double>, constant: Double): LPAffExpr<Double> =
        RealAffExpr(terms, constant)

    override fun coerce(expr: LPAffExpr<*>): LPAffExpr<Double> = expr.relax()

    override fun coerce_number(n: Number): Double = n.toDouble()
}
