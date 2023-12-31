package kulp.domains

import ivory.algebra.IntRing
import ivory.order.IntOrder
import kulp.*
import kulp.expressions.IntAffExpr
import kulp.variables.LPInteger
import kulp.variables.LPVar

data object LPIntegralDomain :
    LPDomain<Int>(
        klass = Int::class,
        ring = IntRing,
        order = IntOrder,
    ) {
    // override val klass: KClass<Int> = Int::class
    // override val zero: Int = 0
    // override val one: Int = 1

    context(BindCtx)
    override fun newvar(lb: Int?, ub: Int?): LPVar<Int> = LPInteger(lb, ub)

    override fun newexpr(terms: Map<LPPath, Int>, constant: Int): LPAffExpr<Int> =
        IntAffExpr(terms, constant)

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
            is Long -> {
                if (n > Int.MAX_VALUE || n < Int.MIN_VALUE) {
                    throw IllegalArgumentException("Cannot coerce $n to Int")
                }
                n.toInt()
            }
            is Float,
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
}
