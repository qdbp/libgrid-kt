package kulp

import kulp.expressions.IntAffExpr
import kulp.expressions.RealAffExpr
import kulp.variables.LPVar
import nullable_fold
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.reflect.KClass

// genericized operations on numbers
@Suppress("UnusedReceiverParameter")
inline fun <reified N : Number> KClass<N>.zero(): N {
    return when (N::class) {
        Double::class -> 0.0
        Float::class -> 0.0f
        Int::class -> 0
        Long::class -> 0L
        else -> throw NotImplementedError()
    }
        as N
}

@Suppress("UnusedReceiverParameter")
inline fun <reified N : Number> KClass<N>.one(): N {
    return when (N::class) {
        Double::class -> 1.0
        Float::class -> 1.0f
        Int::class -> 1
        Long::class -> 1L
        else -> throw NotImplementedError()
    }
        as N
}

inline operator fun <reified N : Number> N.times(other: N): N {
    return when (N::class) {
        Double::class -> this.toDouble() * other.toDouble()
        Float::class -> this.toFloat() * other.toFloat()
        Int::class -> this.toInt() * other.toInt()
        Long::class -> this.toLong() * other.toLong()
        else -> throw NotImplementedError()
    }
        as N
}

inline operator fun <reified N : Number> N.plus(other: N): N {
    return when (N::class) {
        Double::class -> this.toDouble() + other.toDouble()
        Float::class -> this.toFloat() + other.toFloat()
        Int::class -> this.toInt() + other.toInt()
        Long::class -> this.toLong() + other.toLong()
        else -> throw NotImplementedError()
    }
        as N
}

inline operator fun <reified N : Number> N.minus(other: N): N {
    return when (N::class) {
        Double::class -> this.toDouble() - other.toDouble()
        Float::class -> this.toFloat() - other.toFloat()
        Int::class -> this.toInt() - other.toInt()
        Long::class -> this.toLong() - other.toLong()
        else -> throw NotImplementedError()
    }
        as N
}

// int specializations
@Suppress("UNCHECKED_CAST")
inline fun <reified N : Number> Iterable<LPAffExpr<N>>.lp_sum(): LPAffExpr<N> {
    val sum_terms: MutableMap<LPPath, N> = mutableMapOf()
    var constant = (N::class::zero)()

    for (term in this) {
        for ((k, v) in term.terms) {
            sum_terms[k] = (sum_terms[k] ?: (N::class::zero)()) + v
        }
        constant += term.constant
    }

    return when (N::class) {
        Double::class ->
            RealAffExpr(sum_terms.mapValues { it.value.toDouble() }, constant.toDouble())
        Float::class ->
            RealAffExpr(sum_terms.mapValues { it.value.toDouble() }, constant.toDouble())
        Int::class -> IntAffExpr(sum_terms.mapValues { it.value.toInt() }, constant.toInt())
        Long::class -> IntAffExpr(sum_terms.mapValues { it.value.toInt() }, constant.toInt())
        else -> throw NotImplementedError()
    }
        as LPAffExpr<N>
}

inline fun <reified N : Number> Iterable<Pair<LPAffExpr<N>, N>>.lp_dot(): LPAffExpr<N> {
    return this.map { it.first * it.second }.lp_sum()
}

inline fun <reified N : Number> Map<out LPAffExpr<N>, N>.lp_dot(): LPAffExpr<N> {
    return this.entries.map { Pair(it.key, it.value) }.lp_dot()
}

inline fun <reified N : Number> Iterable<LPAffExpr<N>>.lp_dot(values: Iterable<N>): LPAffExpr<N> {
    return this.zip(values).lp_dot()
}

// extensions of Number to handle LPAffExpr
operator fun Double.plus(other: LPAffExpr<Double>): LPAffExpr<Double> = other + this

operator fun Int.plus(other: LPAffExpr<Int>): LPAffExpr<Int> = other + this

operator fun Double.minus(other: LPAffExpr<Double>): LPAffExpr<Double> = (-other) + this

operator fun Int.minus(other: LPAffExpr<Int>): LPAffExpr<Int> = (-other) + this

operator fun Double.times(other: LPAffExpr<Double>): LPAffExpr<Double> = other * this

operator fun Int.times(other: LPAffExpr<Int>): LPAffExpr<Int> = other * this

fun Number.is_nearly_int(): Boolean {
    return abs(this.toDouble() - this.toDouble().roundToInt()) < 1e-6
}

fun Number.roundToInt(): Int {
    return this.toDouble().roundToInt()
}

// great artists steal and all that
fun String.join(it: List<Any>): String = it.joinToString(this)

fun <N : Number> List<LPVar<N>>.lb(): N? {
    when (this.size) {
        0 -> return null
    }
    val domain = this[0].dom
    return this.map { it.lb }.nullable_fold(domain::max)
}

fun <N : Number> List<LPVar<N>>.ub(): N? {
    when (this.size) {
        0 -> return null
    }
    val domain = this[0].dom
    return this.map { it.ub }.nullable_fold(domain::min)
}
