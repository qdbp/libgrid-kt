package kulp.adapters

import kulp.LPProblem
import kulp.LPSolution

fun interface LPSolver {
    fun LPProblem.solve(): LPSolution
}
