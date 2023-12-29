package test_kulp.test_ril

import kulp.LPAffExpr
import kulp.LPObjectiveSense
import kulp.LPProblem
import kulp.LPSolution
import kulp.variables.LPInteger
import test_kulp.TestLPProblem
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

abstract class RILProblem(val mk_objective: (LPAffExpr<Int>, LPAffExpr<Int>) -> LPAffExpr<Int>) :
    TestLPProblem() {
    val x = node { "x" { LPInteger(-10, 10) } }
    val y = node { "y" { LPInteger(-10, 10) } }
    abstract val witness: LPAffExpr<Int>

    override fun get_objective(): Pair<LPAffExpr<*>, LPObjectiveSense> =
        mk_objective(x, y) to LPObjectiveSense.Maximize
}

context(LPSolution, LPProblem)
fun assertRilTrue(expr: LPAffExpr<Int>) {
    val expr_val = value_of(expr)
    assertNotNull(expr_val)
    assertTrue(expr_val >= 1.0, "expected $expr >= 1, got $expr_val")
}

context(LPSolution, LPProblem)
fun assertRilFalse(expr: LPAffExpr<Int>) {
    val expr_val = value_of(expr)
    assertNotNull(expr_val)
    assertTrue(expr_val <= 0.0, "expected $expr <= 0, got $expr_val")
}
