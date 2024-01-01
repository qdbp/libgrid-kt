package test_kulp

import kulp.LPNode
import kulp.LPProblem
import kulp.LPSolution
import kulp.LPSolutionStatus
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class TestLPProblem : LPProblem() {
    // expose root to tests
    fun root(): LPNode = node

    // default null objective
    override fun get_objective() = null_objective
}

context(LPSolution)
fun assertOptimal() = assertEquals(LPSolutionStatus.Optimal, status())

context(LPSolution)
fun assertInfeasible() = assertEquals(LPSolutionStatus.Infeasible, status())

context(LPSolution)
fun assertObjective(target: Double) {
    assertOptimal()
    val obj = objective_value()
    assertEquals(target, obj, 1e-4, "expected objective ~ $target; got $obj")
}

context(LPSolution)
fun assertObjectiveAtLeast(target: Double) {
    assertOptimal()
    val obj = objective_value()
    assertTrue(obj >= target, "expected objective >= $target; got $obj")
}

context(LPSolution)
fun assertObjectiveAtMost(target: Double) {
    assertOptimal()
    val obj = objective_value()
    assertTrue(obj <= target, "expected objective <= $target; got $obj")
}
