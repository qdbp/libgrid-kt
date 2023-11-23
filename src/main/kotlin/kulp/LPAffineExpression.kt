package kulp

import kulp.variables.LPVariable


/**
 * Represents an affine expresison of terms of the following form:
 *
 *    a_1 * x_1 + a_2 * x_2 + ... + a_n * x_n + c
 *
 */
data class LPAffineExpression(
    val terms: Map<LPVariable, Double>,
    val constant: Double
): LPExprLike {

    override fun as_expr(): LPAffineExpression = this

    constructor(constant: Number):  this(mapOf(), constant.toDouble())
    constructor(): this(0.0)

    operator fun unaryMinus(): LPAffineExpression {
        return LPAffineExpression(
            terms.mapValues { -it.value },
            -constant
        )
    }

    operator fun plus(other: Number): LPAffineExpression {
        return LPAffineExpression(
            terms,
            constant + other.toDouble()
        )
    }

    operator fun plus(other: LPAffineExpression): LPAffineExpression {
        val new_terms = terms.toMutableMap()
        for ((k, v) in other.terms) {
            new_terms[k] = (new_terms[k] ?: 0.0) + v
        }
        return LPAffineExpression(
            new_terms,
            constant + other.constant
        )
    }

    operator fun minus(other: Number): LPAffineExpression {
        return this + (-other.toDouble())
    }

    operator fun minus(other: LPAffineExpression): LPAffineExpression {
        return this + (-other)
    }

    operator fun times(other: Number): LPAffineExpression {
        return LPAffineExpression(
            terms.mapValues { it.value * other.toDouble() },
            constant * other.toDouble()
        )
    }

    operator fun div(other: Number): LPAffineExpression {
        return LPAffineExpression(
            terms.mapValues { it.value / other.toDouble() },
            constant / other.toDouble()
        )
    }

}
