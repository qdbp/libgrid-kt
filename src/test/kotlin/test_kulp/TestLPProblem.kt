package test_kulp

import kulp.LPNode
import kulp.LPProblem
import kulp.LPSolution
import kulp.LPSolutionStatus
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.assertEquals

abstract class TestLPProblem : LPProblem() {
    // expose root to tests
    fun root(): LPNode = node
}

context(LPSolution)
fun assertObjective(target: Double) {
    val obj = objective_value()
    assertEquals(target, obj, 1e-4, "expected objective ~ $target; got $obj")
}

context(LPSolution)
fun assertObjectiveAtLeast(target: Double) {
    val obj = objective_value()
    assertTrue(obj >= target, "expected objective >= $target; got $obj")
}

context(LPSolution)
fun assertObjectiveAtMost(target: Double) {
    val obj = objective_value()
    assertTrue(obj <= target, "expected objective <= $target; got $obj")
}

context(LPSolution)
fun assertOptimal() = assertEquals(LPSolutionStatus.Optimal, status())

context(LPSolution)
fun assertInfeasible() = assertEquals(LPSolutionStatus.Infeasible, status())
