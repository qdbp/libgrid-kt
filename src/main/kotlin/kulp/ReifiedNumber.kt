package kulp

import kulp.variables.LPInteger
import kulp.variables.LPReal
import model.LPName
import kotlin.reflect.KClass

// TODO hacky garbage ahead. I hate type erasure and am bad at Kotlin. sorry.
/**
 * Wrapper around KCLass<Number> instances to carry reified number types with us everywhere we might
 * need them.
 *
 * This is buried deeply into the marrow of LPAffExpr to remain both pervasively available and
 * unobtrusive.
 *
 * Most object will not need to touch this -- the intention is to support maximum code sharing for
 * specializations of generic renderables (i.e. Abs<N: Number>).
 */
sealed interface ReifiedNumberTypeWrapper<N : Number> {
    val klass: KClass<N>

    fun new_self_type_lpvar(name: LPName, lb: N? = null, ub: N? = null): LPVariable<N>

    val zero: N

    val one: N

    /**
     * Things are never happy when there's a function called "coerce" in the offing...
     *
     * However, we keep all the quite static and unchanging implementation details of when this is
     * used deeply under control in [LPAffExpr].
     *
     * This function should never need to be called by implementing classes directly.
     */
    fun coerce(expr: LPAffExpr<*>): LPAffExpr<N>

    /** Ditto, but for numbers. */
    fun coerce_number(n: Number): N
}

object IntWrapper : ReifiedNumberTypeWrapper<Int> {
    override val klass: KClass<Int> = Int::class

    override fun new_self_type_lpvar(name: LPName, lb: Int?, ub: Int?): LPVariable<Int> =
        LPInteger(name, lb, ub)

    override val zero: Int = 0

    override val one: Int = 1

    override fun coerce(expr: LPAffExpr<*>): LPAffExpr<Int> {
        @Suppress("UNCHECKED_CAST")
        return when (expr.klass) {
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
}

object DoubleWrapper : ReifiedNumberTypeWrapper<Double> {
    override val klass: KClass<Double> = Double::class

    override fun new_self_type_lpvar(name: LPName, lb: Double?, ub: Double?): LPVariable<Double> =
        LPReal(name, lb, ub)

    override val zero: Double = 0.0

    override val one: Double = 1.0

    override fun coerce(expr: LPAffExpr<*>): LPAffExpr<Double> = expr.relax()

    override fun coerce_number(n: Number): Double = n.toDouble()
}
