package kulp

import kulp.variables.LPVariable

abstract class LPSolution {

    abstract fun status(): LPSolutionStatus

    abstract fun objective_value(): Double

    abstract fun value_of(name: String): Double?

    fun value_of(v: LPVariable): Double? = value_of(v.name)
}
