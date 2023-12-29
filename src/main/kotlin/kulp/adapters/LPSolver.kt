package kulp.adapters

import kulp.LPProblem
import kulp.LPSolution

fun interface LPSolver {
    fun solve(prob: LPProblem): LPSolution
}
