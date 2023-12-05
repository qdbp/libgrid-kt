package kulp

import kulp.transforms.Constrained
import model.LPName

/**
 * Represents an affine expression of terms of the following form:
 *
 * a_1 * x_1 + a_2 * x_2 + ... + a_n * x_n + c
 *
 * The base interface parameterizes over the numerical domain of the expression.
 */
interface LPAffExpr<N : Number> {

    val terms: Map<LPName, N>
    val constant: N

    // override fun as_expr(): LPAffineExpression<N> = this

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

    /** Returns a Constrain-wrapped variable that is constrained to equal this expression.
     *
     *  Cost:
     *  - 1 output variable of the same type as the expression
     *  - 2 constraints for the EQ pin
     *  - a pinch of your dignity as a 1337 haxx0r for not being able to get away with using the
     *    expression directly
     * */
    fun reify(name: LPName): Constrained<N>

    /**
     * Evaluates the expression as written to a number outside a solver context
     *
     * This can and should be used to use the LP business logic to perform out-of-solver evaluations
     * like rendering partial or manual solutions, etc.
     *
     * Should return null when the expression is not fully defined by the assignment.
     */
    fun evaluate(assignment: Map<LPName, N>): N?
}
