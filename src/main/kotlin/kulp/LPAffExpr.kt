package kulp

import kulp.constraints.LP_EQZ
import kulp.constraints.LP_LEZ
import kulp.transforms.Constrained
import model.LPName

/**
 * Represents an affine expression of terms of the following form:
 *
 * a_1 * x_1 + a_2 * x_2 + ... + a_n * x_n + c
 *
 * The base interface parameterizes over the numerical domain of the expression.
 */
interface LPAffExpr<N : Number> : ReifiedNumberTypeWrapper<N> {

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

    /** Creates a new (unbound) constraint: this >= other */
    infix fun le(other: LPAffExpr<out Number>): UnboundRenderable<LPConstraint> {
        // tunnel into NumberInfo
        val coerced = coerce(other)
        return object : UnboundRenderable<LPConstraint> {
            override fun bind(name: LPName): LPConstraint = LP_LEZ(name, this@LPAffExpr - coerced)
        }
    }

    /** Creates a new (unbound) constraint: this >= other */
    infix fun le(other: Number): UnboundRenderable<LPConstraint> =
        this le as_expr(coerce_number(other))

    /** Creates a new (unbound) constraint: this <= 0 */
    fun lez(): UnboundRenderable<LPConstraint> {
        return object : UnboundRenderable<LPConstraint> {
            override fun bind(name: LPName): LPConstraint = LP_LEZ(name, this@LPAffExpr)
        }
    }

    /** Creates a new (unbound) constraint: this <= other */
    infix fun ge(other: LPAffExpr<out Number>): UnboundRenderable<LPConstraint> =
        (-this) le (-other)

    /** Creates a new (unbound) constraint: this <= other */
    infix fun ge(other: Number): UnboundRenderable<LPConstraint> =
        this ge as_expr(coerce_number(other))

    /** Creates a new (unbound) constraint: this >= 0 */
    fun gez(): UnboundRenderable<LPConstraint> = (-this).lez()

    /** Creates a new (unbound) constraint: this == other */
    infix fun eq(other: LPAffExpr<N>): UnboundRenderable<LPConstraint> {
        return object : UnboundRenderable<LPConstraint> {
            override fun bind(name: LPName): LPConstraint = LP_EQZ(name, this@LPAffExpr - other)
        }
    }

    /** Creates a new (unbound) constraint this == other */
    infix fun eq(other: Number): UnboundRenderable<LPConstraint> =
        this eq as_expr(coerce_number(other))

    /** Creates a new (unbound) constraint: this == 0 */
    fun eqz(): UnboundRenderable<LPConstraint> = this eq as_expr(zero)
}

/**
 * A wrapping interface around, effectively, a closure that requires a name to finish producing a
 * renderable. Combined with the LPName receiver on [LPRenderable.decompose], this exposes a natural
 * "builder" style syntax for generating related lists of renderables.
 */
interface UnboundRenderable<T : LPRenderable> {
    fun bind(name: LPName): T

    // TODO need to abstract over "refinable" types
    context(LPName)
    infix fun named(s: String): T = bind(this@LPName.refine(s))

    context(LPName)
    infix fun named(indices: List<Int>): T = bind(this@LPName.refine(indices))

    infix fun named(s: LPName): T = bind(s)

    /**
     * Binds the renderable to an absolute name serving as the root of a new tree.
     *
     * This is useful inside Problems, where we define a lot of top level variables.
     */
    infix fun rooted(s: String): T = bind(LPName(s))
}
