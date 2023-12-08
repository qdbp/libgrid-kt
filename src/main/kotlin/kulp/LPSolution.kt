package kulp

abstract class LPSolution {

    abstract fun status(): LPSolutionStatus

    abstract fun objective_value(): Double

    abstract fun value_of(name: LPNode): Double?

    fun value_of(rnd: LPRenderable): Double? = value_of(rnd.node)
}
