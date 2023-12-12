package kulp.domains

import kotlin.reflect.KClass
import kulp.BindCtx
import kulp.LPAffExpr
import kulp.LPNode
import kulp.expressions.RealAffExpr
import kulp.variables.LPReal
import kulp.variables.LPVar

object Real : LPDomainImpl<Double>() {
    override val klass: KClass<Double> = Double::class
    override val zero: Double = 0.0
    override val one: Double = 1.0

    override fun unsafe_node_as_expr(node: LPNode): LPAffExpr<Double> =
        RealAffExpr(mapOf(node.path to 1.0), 0.0)

    context(BindCtx)
    override fun newvar(lb: Double?, ub: Double?): LPVar<Double> = LPReal(lb, ub)

    override fun coerce(expr: LPAffExpr<*>): LPAffExpr<Double> = expr.relax()

    override fun coerce_number(n: Number): Double = n.toDouble()

    override val max: (Double, Double) -> Double = { a, b -> maxOf(a, b) }
    override val min: (Double, Double) -> Double = { a, b -> minOf(a, b) }

    override val mul: (n: Double, m: Double) -> Double = { n, m -> n * m }
    override val add: (n: Double, m: Double) -> Double = { n, m -> n + m }
}
