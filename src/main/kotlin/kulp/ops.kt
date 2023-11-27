package kulp

import kulp.variables.LPBinary
import model.SegName

fun Iterable<LPExprLike>.lp_sum(): LPAffineExpression {
    val sum_terms: MutableMap<SegName, Double> = mutableMapOf()
    var constant = 0.0

    for (term in this) {
        val affine_term = term.as_expr()
        for ((k, v) in affine_term.terms) {
            sum_terms[k] = (sum_terms[k] ?: 0.0) + v
        }
        constant += affine_term.constant
    }

    return LPAffineExpression(sum_terms, constant)
}

fun Iterable<Pair<LPExprLike, Number>>.lp_dot(): LPAffineExpression {
    val sum_terms: MutableMap<SegName, Double> = mutableMapOf()
    var constant = 0.0

    for ((first, second) in this) {
        val affine_term = first.as_expr()
        for ((k, v) in affine_term.terms) {
            sum_terms[k] = (sum_terms[k] ?: 0.0) + v * second.toDouble()
        }
        constant += affine_term.constant * second.toDouble()
    }

    return LPAffineExpression(sum_terms, constant)
}

fun Map<out LPExprLike, Number>.lp_dot(): LPAffineExpression {
    return this.entries.map { Pair(it.key, it.value) }.lp_dot()
}

fun Iterable<LPExprLike>.lp_dot(values: Iterable<Number>): LPAffineExpression {
    return this.zip(values).lp_dot()
}

// mathematical operation
fun lp_implies(x: LPBinary, y: LPBinary): LPAffineExpression {
    return !x + y
}

// extensions of Number to handle LPExprLike
operator fun Number.plus(other: LPExprLike): LPAffineExpression = other.as_expr() + this.toDouble()

operator fun Number.minus(other: LPExprLike): LPAffineExpression =
    -other.as_expr() + this.toDouble()

operator fun Number.times(other: LPExprLike): LPAffineExpression = other.as_expr() * this.toDouble()
