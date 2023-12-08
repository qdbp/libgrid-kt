package kulp

import kotlin.reflect.KClass
import kulp.variables.LPInteger
import kulp.variables.LPReal
import kulp.variables.LPVar

sealed class LPDomain<N : Number> {
    abstract val klass: KClass<N>
    abstract val zero: N
    abstract val one: N

    abstract fun newvar(node: LPNode, lb: N? = null, ub: N? = null): LPVar<N>

    /**
     * this is a dangerous function that can lead to nonsense if misused (aka making an affine
     * expression of constraints). However, we need this in some very particular places to avoid
     * type erasure pain.
     */
    abstract fun unsafe_node_as_expr(node: LPNode): LPAffExpr<N>

    /**
     * Things are never happy when there's a function called "coerce" in the offing...
     *
     * However, we keep all the quite static and unchanging implementation details of when this is
     * used deeply under control in [LPAffExpr].
     *
     * This function should never need to be called by implementing classes directly.
     */
    abstract fun coerce(expr: LPAffExpr<*>): LPAffExpr<N>

    /** Ditto, but for numbers. */
    abstract fun coerce_number(n: Number): N

    abstract val max: (N, N) -> N

    abstract val min: (N, N) -> N
}

object Real : LPDomain<Double>() {
    override val klass: KClass<Double> = Double::class
    override val zero: Double = 0.0
    override val one: Double = 1.0

    override fun unsafe_node_as_expr(node: LPNode): LPAffExpr<Double> =
        RealAffExpr(mapOf(node to 1.0), 0.0)

    override fun newvar(node: LPNode, lb: Double?, ub: Double?): LPVar<Double> =
        LPReal(node, lb, ub)

    override fun coerce(expr: LPAffExpr<*>): LPAffExpr<Double> = expr.relax()

    override fun coerce_number(n: Number): Double = n.toDouble()

    override val max: (Double, Double) -> Double = { a, b -> maxOf(a, b) }
    override val min: (Double, Double) -> Double = { a, b -> minOf(a, b) }
}

object Integral : LPDomain<Int>() {
    override val klass: KClass<Int> = Int::class
    override val zero: Int = 0
    override val one: Int = 1

    override fun unsafe_node_as_expr(node: LPNode): LPAffExpr<Int> = IntAffExpr(mapOf(node to 1), 0)

    override fun newvar(node: LPNode, lb: Int?, ub: Int?): LPVar<Int> = LPInteger(node, lb, ub)

    override fun coerce(expr: LPAffExpr<*>): LPAffExpr<Int> {
        @Suppress("UNCHECKED_CAST")
        return when (expr.domain.klass) {
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
}
