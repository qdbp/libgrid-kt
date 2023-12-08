package kulp

/** Represents an affine expression with real coefficients, constants and variables. */
data class RealAffExpr(override val terms: Map<LPNode, Double>, override val constant: Double) :
    LPAffExpr<Double> {

    constructor(constant: Number) : this(mapOf(), constant.toDouble())

    constructor() : this(0.0)

    override val domain: LPDomain<Double> = Real

    override fun as_expr(n: Double): LPAffExpr<Double> = RealAffExpr(n)

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
        val new_terms = mutableMapOf<LPNode, Double>()
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

    override fun evaluate(assignment: Map<LPNode, Double>): Double? {
        var result = constant
        for ((name, coef) in terms) {
            assignment[name]?.let { result += it * coef } ?: return null
        }
        return result
    }
}
