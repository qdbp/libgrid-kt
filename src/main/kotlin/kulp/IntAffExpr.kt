package kulp

import kulp.constraints.LP_EQZ
import kulp.transforms.Constrained
import kulp.transforms.IntClip
import kulp.variables.LPInteger
import model.LPName

// extensions on LPAffExpr<Int> to be more generic
fun LPAffExpr<Int>.int_clip(name: LPName, lb: Int?, ub: Int?): IntClip = IntClip(name, this, lb, ub)

fun LPAffExpr<Int>.bool_clip(name: LPName): IntClip = int_clip(name, 0, 1)

// int expressions support strict inequality
infix fun LPAffExpr<Int>.lt(other: LPAffExpr<Int>): UnboundRenderable<LPConstraint> {
    return this le (other - 1)
}

infix fun LPAffExpr<Int>.lt(other: Int): UnboundRenderable<LPConstraint> {
    return this le (other - 1)
}

infix fun LPAffExpr<Int>.gt(other: LPAffExpr<Int>): UnboundRenderable<LPConstraint> =
    this ge (other + 1)

infix fun LPAffExpr<Int>.gt(other: Int): UnboundRenderable<LPConstraint> = this ge (other + 1)

fun LPAffExpr<Int>.ltz(): UnboundRenderable<LPConstraint> = this le -1

fun LPAffExpr<Int>.gtz(): UnboundRenderable<LPConstraint> = this ge 1

data class IntAffExpr(override val terms: Map<LPName, Int>, override val constant: Int) :
    LPAffExpr<Int>, ReifiedNumberTypeWrapper<Int> by IntWrapper {

    constructor(constant: Number) : this(mapOf(), constant.toInt())

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
        val new_terms = mutableMapOf<LPName, Int>()
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

    override fun reify(name: LPName): Constrained<Int> {
        return LPInteger(name) requiring { LP_EQZ(name.refine("reify_pin"), this - it) }
    }

    override fun evaluate(assignment: Map<LPName, Int>): Int? {
        var result = constant
        for ((name, coef) in terms) {
            assignment[name]?.let { result += it * coef } ?: return null
        }
        return result
    }
}
