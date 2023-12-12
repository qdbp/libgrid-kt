package kulp

// TODO might want to make generic to support the fully integeral pipeline
abstract class LPSolution {

    abstract fun status(): LPSolutionStatus

    abstract fun objective_value(): Double

    abstract fun value_of(path: LPPath): Double?

    fun value_of(node: LPNode): Double? = value_of(node.path)

    fun value_of(expr: LPAffExpr<*>): Double? {
        var out = expr.constant.toDouble()
        expr.terms.forEach { (path, coef) ->
            value_of(path)?.let { out += coef.toDouble() * it } ?: return null
        }
        return out
    }
}
