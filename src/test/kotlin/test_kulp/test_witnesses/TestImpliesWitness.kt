package test_kulp.test_witnesses

import kotlin.test.Test
import kotlin.test.assertEquals
import kulp.LPAffExpr
import kulp.LPSolutionStatus
import kulp.transforms.ril.RIL
import kulp.use
import org.junit.jupiter.api.Assertions.assertTrue
import test_kulp.ScipTester

private class ImpliesWitnessProblem(
    mk_objective: (LPAffExpr<Int>, LPAffExpr<Int>) -> LPAffExpr<Int>
) : WitnessProblem(mk_objective) {

    override val witness = node { RIL.implies(x, y) }
}

class TestImpliesWitness : ScipTester() {

    @Test
    fun witnesses_true_not_p() {
        val prob = ImpliesWitnessProblem { x, y -> -x - y }
        val solution = solve_problem(prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(-10.0, solution.value_of(prob.x))
        assertEquals(-10.0, solution.value_of(prob.y))
        assertTrue(solution.value_of(prob.witness)!! >= 1)
    }

    @Test
    fun witnesses_true_q() {
        val prob = ImpliesWitnessProblem { x, y -> x + y }
        val solution = solve_problem(prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(10.0, solution.value_of(prob.x))
        assertEquals(10.0, solution.value_of(prob.y))
        assertTrue(solution.value_of(prob.witness)!! >= 1.0)
    }

    @Test
    fun witnesses_false() {
        val prob = ImpliesWitnessProblem { x, y -> x - y }
        val solution = solve_problem(prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(10.0, solution.value_of(prob.x))
        assertEquals(-10.0, solution.value_of(prob.y))
        assertTrue(solution.value_of(prob.witness)!! <= 0)
    }

    @Test
    fun binds_feasible() {
        val prob = ImpliesWitnessProblem { x, y -> x - y }
        prob use { "test_bind_pin" { witness eq 1 } }
        val solution = solve_problem(prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        // can get this value by either not p or q route, but it's always 9
        assertEquals(9.0, solution.objective_value())
        assertEquals(1.0, solution.value_of(prob.witness))
    }

    @Test
    fun binds_infeasible() {
        val prob = ImpliesWitnessProblem { x, y -> x + y }
        prob use { "test_pin_x" { x ge 1 } }
        prob use { "test_pin_y" { y le 0 } }
        prob use { "test_bind_pin" { witness eq 1 } }
        val solution = solve_problem(prob)
        assertEquals(LPSolutionStatus.Infeasible, solution.status())
    }
}
