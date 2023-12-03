package kulp

import kotlin.math.roundToInt
import kulp.constraints.LP_EQ
import kulp.variables.LPInteger
import kulp.variables.LPReal
import model.SegName

/**
 * Represents an affine expression of terms of the following form:
 *
 * a_1 * x_1 + a_2 * x_2 + ... + a_n * x_n + c
 */
interface LPAffExpr<N : Number> {

    val terms: Map<SegName, N>
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

    /** Returns a transform-wrapped that is constrained to equal this expression. */
    fun reify(name: SegName): LPTransform<N>
}

/** Represents an affine expression with real coefficients, constants and variables. */
data class RealAffExpr(override val terms: Map<SegName, Double>, override val constant: Double) :
    LPAffExpr<Double> {

    constructor(constant: Number) : this(mapOf(), constant.toDouble())

    constructor() : this(0.0)

    override operator fun unaryMinus(): RealAffExpr {
        return RealAffExpr(terms.mapValues { -it.value }, -constant)
    }

    override fun times(other: Double): LPAffExpr<Double> {
        return RealAffExpr(terms.mapValues { it.value * other }, constant * other)
    }

    override fun minus(other: LPAffExpr<Double>): LPAffExpr<Double> {
        return this + (-other)
    }

    override fun minus(other: Double): LPAffExpr<Double> {
        return this + (-other)
    }

    override fun plus(other: LPAffExpr<Double>): LPAffExpr<Double> {
        val new_terms = mutableMapOf<SegName, Double>()
        for ((k, v) in terms) {
            new_terms[k] = v
        }
        for ((k, v) in other.terms) {
            new_terms[k] = (new_terms[k] ?: 0.0) + v
        }
        return RealAffExpr(new_terms, constant + other.constant)
    }

    override fun plus(other: Double): LPAffExpr<Double> {
        return RealAffExpr(terms, constant + other)
    }

    fun try_as_int(): LPAffExpr<Int>? {
        if (!terms.values.all { it.is_nearly_int() } || !constant.is_nearly_int()) {
            return null
        }
        return IntAffExpr(terms.mapValues { it.value.roundToInt() }, constant.roundToInt())
    }

    override fun reify(name: SegName): LPTransform<Double> {
        val variable = LPReal(name)
        return object : LPTransform<Double>(variable) {
            override fun render_auxiliaries(ctx: MipContext): List<LPRenderable> {
                return listOf(LP_EQ(name.refine("reify_pin"), variable, this@RealAffExpr))
            }
        }
    }
}

data class IntAffExpr(override val terms: Map<SegName, Int>, override val constant: Int) :
    LPAffExpr<Int> {

    constructor(constant: Number) : this(mapOf(), constant.toInt())

    constructor() : this(0)

    override fun unaryMinus(): LPAffExpr<Int> {
        return IntAffExpr(terms.mapValues { -it.value }, -constant)
    }

    override fun times(other: Int): LPAffExpr<Int> {
        return IntAffExpr(terms.mapValues { it.value * other }, constant * other)
    }

    // TODO this might cause more problems than its worth; if it does we should remove div
    // from the interface definition and make it a special method just for RealAffineExpression
    fun int_div(other: Int): LPAffExpr<Int> {
        require(terms.values.all { it % other == 0 }) {
            "Dividing an int expression requires that all terms be divisible $this by $other"
        }
        require(constant % other == 0) {
            "Dividing an int expression requires that the constant term be divisible $this by $other"
        }
        return IntAffExpr(terms.mapValues { it.value / other }, constant / other)
    }

    override fun minus(other: LPAffExpr<Int>): LPAffExpr<Int> {
        return this + (-other)
    }

    override fun minus(other: Int): LPAffExpr<Int> {
        return this + (-other)
    }

    override fun plus(other: LPAffExpr<Int>): LPAffExpr<Int> {
        val new_terms = mutableMapOf<SegName, Int>()
        for ((k, v) in terms) {
            new_terms[k] = v
        }
        for ((k, v) in other.terms) {
            new_terms[k] = (new_terms[k] ?: 0) + v
        }
        return IntAffExpr(new_terms, constant + other.constant)
    }

    override fun plus(other: Int): LPAffExpr<Int> {
        return IntAffExpr(terms, constant + other)
    }

    override fun reify(name: SegName): LPTransform<Int> {
        val variable = LPInteger(name)
        return object : LPTransform<Int>(variable) {
            override fun render_auxiliaries(ctx: MipContext): List<LPRenderable> {
                return listOf(LP_EQ(name.refine("reify_pin"), variable, this@IntAffExpr))
            }
        }
    }
}
