package kulp.domains

import kotlin.reflect.KClass
import kulp.*
import kulp.expressions.IntAffExpr
import kulp.variables.LPInteger
import kulp.variables.LPVar

object Integral : LPDomainImpl<Int>() {
    override val klass: KClass<Int> = Int::class
    override val zero: Int = 0
    override val one: Int = 1

    override fun unsafe_node_as_expr(node: LPNode): LPAffExpr<Int> =
        IntAffExpr(mapOf(node.path to 1), 0)

    context(BindCtx)
    override fun newvar(lb: Int?, ub: Int?): LPVar<Int> = LPInteger(lb, ub)

    override fun coerce(expr: LPAffExpr<*>): LPAffExpr<Int> {
        @Suppress("UNCHECKED_CAST")
        return when (expr.dom.klass) {
            Int::class -> expr as LPAffExpr<Int>
            // TODO might want to disallow this entirely
            Double::class -> expr.as_int()
            else -> throw NotImplementedError()
        }
    }

    override fun coerce_number(n: Number): Int {
        return when (n) {
            is Int -> n
            is Double -> {
                // TODO might want to disallow this entirely
                if (!n.is_nearly_int()) {
                    throw IllegalArgumentException("Cannot coerce non-int like $n to Int")
                }
                n.roundToInt()
            }
            else -> throw NotImplementedError()
        }
    }

    override val max: (Int, Int) -> Int = { a, b -> maxOf(a, b) }
    override val min: (Int, Int) -> Int = { a, b -> minOf(a, b) }

    override val mul: (n: Int, m: Int) -> Int = { n, m -> n * m }
    override val add: (n: Int, m: Int) -> Int = { n, m -> n + m }
}
