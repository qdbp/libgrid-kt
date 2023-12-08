package kulp

import kulp.constraints.LP_EQZ
import kulp.constraints.LP_LEZ
import kulp.variables.LPVar

/**
 * Represents an abstract affine expression that can be expressed as a form:
 *
 * a_1 * x_1 + a_2 * x_2 + ... + a_n * x_n + c
 *
 * The base interface parameterizes over the numerical domain of the expression.
 */
interface LPAffExpr<N : Number> {

    val domain: LPDomain<N>
    val terms: Map<LPNode, N>
    val constant: N

    companion object {
        // gets an empty expression of the associated typejj
        inline fun <reified M : Number> get_empty(): LPAffExpr<M> {
            @Suppress("UNCHECKED_CAST")
            return when (M::class) {
                Double::class -> RealAffExpr() as LPAffExpr<M>
                Int::class -> IntAffExpr() as LPAffExpr<M>
                else -> throw NotImplementedError()
            }
        }
    }

    /**
     * Any affine expression can be relaxed to a real affine expression.
     *
     * Many times we don't care about keeping an expression integral. This method reduces
     * boilerplate by allowing us to implicitly relax an expression to a real affine expression
     * unless we strictly require a more specific type.
     */
    fun relax(): LPAffExpr<Double> {
        return RealAffExpr(terms.mapValues { it.value.toDouble() }, constant.toDouble())
    }

    fun try_as_int(): LPAffExpr<Int>? {
        if (!terms.values.all { it.is_nearly_int() } || !constant.is_nearly_int()) {
            return null
        }
        return IntAffExpr(terms.mapValues { it.value.roundToInt() }, constant.roundToInt())
    }

    fun as_int(): LPAffExpr<Int> {
        return try_as_int()
            ?: throw IllegalStateException("Cannot convert $this to an integer expression")
    }

    fun as_expr(n: N): LPAffExpr<N>

    /** Yes, this implicitly prohibits unsigned instances of LPAffineExpression. */
    operator fun unaryMinus(): LPAffExpr<N>

    operator fun plus(other: N): LPAffExpr<N>

    operator fun plus(other: LPAffExpr<N>): LPAffExpr<N>

    operator fun minus(other: N): LPAffExpr<N>

    operator fun minus(other: LPAffExpr<N>): LPAffExpr<N>

    operator fun <M : Number> div(other: LPAffExpr<M>): LPAffExpr<Double> {
        return this.relax() / other.relax()
    }

    operator fun times(other: N): LPAffExpr<N>

    operator fun <M : Number> times(other: LPAffExpr<M>): LPAffExpr<Double> {
        return this.relax() * other.relax()
    }

    /**
     * Returns a Constrain-wrapped variable that is constrained to equal this expression.
     *
     * Cost:
     * - 1 output variable of the same type as the expression
     * - 2 constraints for the EQ pin
     * - a pinch of your dignity as a 1337 haxx0r for not being able to get away with using the
     *   expression directly
     */
    fun reify(node: LPNode): LPVar<N> =
        domain.newvar(node) requiring { this eq it named "reify_pin" }

    /**
     * Evaluates the expression as written to a number outside a solver context
     *
     * This can and should be used to use the LP business logic to perform out-of-solver evaluations
     * like rendering partial or manual solutions, etc.
     *
     * Should return null when the expression is not fully defined by the assignment.
     */
    fun evaluate(assignment: Map<LPNode, N>): N?

    /** Creates a new (unbound) constraint: this >= other */
    infix fun le(other: LPAffExpr<out Number>): Free<LPConstraint> {
        // tunnel into NumberInfo
        val coerced = domain.coerce(other)
        return { LP_LEZ(it, this - coerced) }
    }

    /** Creates a new (unbound) constraint: this >= other */
    infix fun le(other: Number): Free<LPConstraint> = this le as_expr(domain.coerce_number(other))

    /** Creates a new (unbound) constraint: this <= 0 */
    val lez: Free<LPConstraint>
        get() = { LP_LEZ(it, this) }

    /** Creates a new (unbound) constraint: this <= other */
    infix fun ge(other: LPAffExpr<out Number>): Free<LPConstraint> = (-this) le (-other)

    /** Creates a new (unbound) constraint: this <= other */
    infix fun ge(other: Number): Free<LPConstraint> = this ge as_expr(domain.coerce_number(other))

    /** Creates a new (unbound) constraint: this >= 0 */
    val gez: Free<LPConstraint>
        get() = (-this).lez

    /** Creates a new (unbound) constraint: this == other */
    infix fun eq(other: LPAffExpr<N>): Free<LPConstraint> = { LP_EQZ(it, this - other) }

    /** Creates a new (unbound) constraint this == other */
    infix fun eq(other: Number): Free<LPConstraint> = this eq as_expr(domain.coerce_number(other))

    /** Creates a new (unbound) constraint: this == 0 */
    val eqz: Free<LPConstraint>
        get() = this eq as_expr(domain.zero)
}

abstract class LPSumExpr<N : Number> : LPAffExpr<N> {
    final override fun toString(): String {
        if (this.terms.size <= 3) {
            val out = terms.entries.joinToString(" + ") { "${it.value} ${it.key}" } + " + $constant"
            return out.replace("+ -", "- ")
        } else {
            return "... + $constant"
        }
    }
}
