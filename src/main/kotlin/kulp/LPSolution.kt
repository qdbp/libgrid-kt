package kulp

import model.SegName

abstract class LPSolution {

    abstract fun status(): LPSolutionStatus

    abstract fun objective_value(): Double

    abstract fun value_of(name: SegName): Double?

    fun value_of(v: LPVariable<*>): Double? = value_of(v.name)
}
