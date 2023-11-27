package kulp

import model.SegName


/**
 * Represents an affine expresison of terms of the following form:
 *
 *    a_1 * x_1 + a_2 * x_2 + ... + a_n * x_n + c
 *
 */
data class LPAffineExpression(
    val terms: Map<SegName, Double>,
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

    operator fun plus(other: LPExprLike): LPAffineExpression {
        val new_terms = terms.toMutableMap()
        val other_expr = other.as_expr()
        for ((k, v) in other_expr.terms) {
            new_terms[k] = (new_terms[k] ?: 0.0) + v
        }
        return LPAffineExpression(
            new_terms,
            constant + other_expr.constant
        )
    }

    operator fun minus(other: Number): LPAffineExpression {
        return this + (-other.toDouble())
    }

    operator fun minus(other: LPExprLike): LPAffineExpression {
        return this + (-other.as_expr())
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
