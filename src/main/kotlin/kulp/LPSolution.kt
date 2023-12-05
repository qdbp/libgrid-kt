package kulp

import model.LPName

abstract class LPSolution {

    abstract fun status(): LPSolutionStatus

    abstract fun objective_value(): Double

    abstract fun value_of(name: LPName): Double?
}
