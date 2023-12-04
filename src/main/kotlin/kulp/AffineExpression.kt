package kulp

import kotlin.math.roundToInt
import kulp.constraints.LP_EQ
import kulp.transforms.Constrained
import kulp.transforms.IntClip
import kulp.variables.LPInteger
import kulp.variables.LPReal
import model.SegName

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

    override fun reify(name: SegName): Constrained<Double> {
        val variable = LPReal(name)
        return Constrained(variable, LP_EQ(name.refine("reify_pin"), variable, this))
    }

    override fun evaluate(assignment: Map<SegName, Double>): Double? {
        var result = constant
        for ((name, coef) in terms) {
            assignment[name]?.let { result += it * coef } ?: return null
        }
        return result
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

    override fun reify(name: SegName): Constrained<Int> {
        val variable = LPInteger(name)
        return Constrained(variable, LP_EQ(name.refine("reify_pin"), variable, this))
    }

    override fun evaluate(assignment: Map<SegName, Int>): Int? {
        var result = constant
        for ((name, coef) in terms) {
            assignment[name]?.let { result += it * coef } ?: return null
        }
        return result
    }
}

// extensions on LPAffExpr<Int> to be more generic
fun LPAffExpr<Int>.int_clip(name: SegName, lb: Int?, ub: Int?): IntClip =
    IntClip(name, this, lb, ub)

fun LPAffExpr<Int>.bool_clip(name: SegName): IntClip = int_clip(name, 0, 1)
