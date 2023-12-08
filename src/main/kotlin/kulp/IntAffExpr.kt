package kulp

import kulp.transforms.IntClip

// extensions on LPAffExpr<Int> to be more generic
fun LPAffExpr<Int>.int_clip(lb: Int? = null, ub: Int? = null): Free<IntClip> = { node ->
    IntClip(node, this, lb, ub)
}

val LPAffExpr<Int>.boolclipped: Free<IntClip>
    get() = int_clip(0, 1)

// int expressions support strict inequality
infix fun LPAffExpr<Int>.lt(other: LPAffExpr<Int>): Free<LPConstraint> {
    return this le (other - 1)
}

infix fun LPAffExpr<Int>.lt(other: Int): Free<LPConstraint> {
    return this le (other - 1)
}

infix fun LPAffExpr<Int>.gt(other: LPAffExpr<Int>): Free<LPConstraint> = this ge (other + 1)

infix fun LPAffExpr<Int>.gt(other: Int): Free<LPConstraint> = this ge (other + 1)

fun LPAffExpr<Int>.ltz(): Free<LPConstraint> = this le -1

fun LPAffExpr<Int>.gtz(): Free<LPConstraint> = this ge 1

data class IntAffExpr(override val terms: Map<LPNode, Int>, override val constant: Int) :
    LPSumExpr<Int>() {

    constructor(constant: Number) : this(mapOf(), constant.toInt())

    constructor() : this(0)

    override val domain: LPDomain<Int> = Integral

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
        val new_terms = mutableMapOf<LPNode, Int>()
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

    override fun evaluate(assignment: Map<LPNode, Int>): Int? {
        var result = constant
        for ((name, coef) in terms) {
            assignment[name]?.let { result += it * coef } ?: return null
        }
        return result
    }
}
