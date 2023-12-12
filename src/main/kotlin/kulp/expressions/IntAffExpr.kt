package kulp.expressions

import kulp.LPAffExpr
import kulp.LPDomain
import kulp.LPPath
import kulp.domains.Integral

data class IntAffExpr(override val terms: Map<LPPath, Int>, override val constant: Int) :
    LPSumExpr<Int>(), LPDomain<Int> by Integral {

    override val dom: LPDomain<Int> = Integral

    constructor(constant: Number) : this(mapOf(), constant.toInt())

    constructor() : this(0)

    override fun as_expr(n: Int): LPAffExpr<Int> = IntAffExpr(n)

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
        val new_terms = mutableMapOf<LPPath, Int>()
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
}
