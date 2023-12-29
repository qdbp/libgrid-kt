package test_kulp.test_ril

import kotlin.test.Test
import kotlin.test.assertEquals
import kulp.LPAffExpr
import kulp.LPSolutionStatus
import kulp.transforms.ril.RIL
import kulp.use
import org.junit.jupiter.api.Assertions.assertTrue
import test_kulp.ScipTester

private class ImpliesRILProblem(
    mk_objective: (LPAffExpr<Int>, LPAffExpr<Int>) -> LPAffExpr<Int>
) : RILProblem(mk_objective) {

    override val witness = node { RIL.implies(x, y) }
}

class TestRILImplies : ScipTester() {

    @Test
    fun witnesses_true_not_p() {
        val prob = ImpliesRILProblem { x, y -> -x - y }
        val solution = solve(prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(-10.0, solution.value_of(prob.x))
        assertEquals(-10.0, solution.value_of(prob.y))
        assertTrue(solution.value_of(prob.witness)!! >= 1)
    }

    @Test
    fun witnesses_true_q() {
        val prob = ImpliesRILProblem { x, y -> x + y }
        val solution = solve(prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(10.0, solution.value_of(prob.x))
        assertEquals(10.0, solution.value_of(prob.y))
        assertTrue(solution.value_of(prob.witness)!! >= 1.0)
    }

    @Test
    fun witnesses_false() {
        val prob = ImpliesRILProblem { x, y -> x - y }
        val solution = solve(prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(10.0, solution.value_of(prob.x))
        assertEquals(-10.0, solution.value_of(prob.y))
        assertTrue(solution.value_of(prob.witness)!! <= 0)
    }

    @Test
    fun binds_feasible() {
        val prob = ImpliesRILProblem { x, y -> x - y * 2 }
        prob use { "test_bind_pin" { witness eq 1 } }
        val solution = solve(prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        // can get this value by either not p or q route, but it's always 9
        assertEquals(0.0, solution.value_of(prob.x))
        assertEquals(-10.0, solution.value_of(prob.y))
        assertEquals(20.0, solution.objective_value())
        assertEquals(1.0, solution.value_of(prob.witness))
    }

    @Test
    fun binds_infeasible() {
        val prob = ImpliesRILProblem { x, y -> x + y }
        prob use { "test_pin_x" { x ge 1 } }
        prob use { "test_pin_y" { y le 0 } }
        prob use { "test_bind_pin" { witness eq 1 } }
        val solution = solve(prob)
        assertEquals(LPSolutionStatus.Infeasible, solution.status())
    }
}
