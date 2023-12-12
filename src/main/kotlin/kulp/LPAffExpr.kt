package kulp

import kulp.constraints.LP_EQZ
import kulp.constraints.LP_LEZ
import kulp.expressions.IntAffExpr
import kulp.expressions.RealAffExpr
import kulp.variables.LPVar

/**
 * Represents an abstract affine expression that can be expressed as a form:
 *
 * a_1 * x_1 + a_2 * x_2 + ... + a_n * x_n + c
 *
 * The base interface parameterizes over the numerical domain of the expression.
 */
interface LPAffExpr<N : Number> {

    val dom: LPDomain<N>
    val terms: Map<LPPath, N>
    val constant: N

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
    context(NodeCtx)
    fun reify(): LPVar<N>

    /** Creates a new (free) constraint: this >= other */
    context(BindCtx)
    infix fun le(other: LPAffExpr<out Number>): LPConstraint {
        // tunnel into NumberInfo
        val coerced = dom.coerce(other)
        return LP_LEZ(this - coerced)
    }

    /** Creates a new  constraint: this >= other */
    context(BindCtx)
    infix fun le(other: Number): LPConstraint = this le as_expr(dom.coerce_number(other))

    /** Creates a new  constraint: this <= 0 */
    context(BindCtx)
    val lez: LPConstraint get() = LP_LEZ(this)

    /** Creates a new  constraint: this <= other */
    context(BindCtx)
    infix fun ge(other: LPAffExpr<out Number>): LPConstraint = (-this) le (-other)

    /** Creates a new  constraint: this <= other */
    context(BindCtx)
    infix fun ge(other: Number): LPConstraint = this ge as_expr(dom.coerce_number(other))

    /** Creates a new  constraint: this >= 0 */
    context(BindCtx)
    val gez: LPConstraint
        get() = (-this).lez

    /** Creates a new (free) constraint: this == other */
    context(BindCtx)
    infix fun eq(other: LPAffExpr<N>): LPConstraint = LP_EQZ(this - other)

    /** Creates a new (free) constraint this == other */
    context(BindCtx)
    infix fun eq(other: Number): LPConstraint = this eq as_expr(dom.coerce_number(other))

    /** Creates a new (free) constraint: this == 0 */
    context(BindCtx)
    val eqz: LPConstraint
        get() = this eq as_expr(dom.zero)
}
