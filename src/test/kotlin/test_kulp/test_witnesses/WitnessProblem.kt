package test_kulp.test_witnesses

import kulp.LPAffExpr
import kulp.LPObjectiveSense
import kulp.variables.LPInteger
import test_kulp.TestLPProblem

abstract class WitnessProblem(
    val mk_objective: (LPAffExpr<Int>, LPAffExpr<Int>) -> LPAffExpr<Int>
) : TestLPProblem() {
    val x = node { "x" { LPInteger(-10, 10) } }
    val y = node { "y" { LPInteger(-10, 10) } }
    abstract val witness: LPAffExpr<Int>

    override fun get_objective(): Pair<LPAffExpr<*>, LPObjectiveSense> =
        mk_objective(x, y) to LPObjectiveSense.Maximize
}
