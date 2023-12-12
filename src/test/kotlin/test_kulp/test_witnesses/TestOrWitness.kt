package test_kulp.test_witnesses

import kotlin.test.Test
import kotlin.test.assertEquals
import kulp.LPAffExpr
import kulp.LPSolutionStatus
import kulp.times
import kulp.transforms.ril.RIL
import kulp.use
import org.junit.jupiter.api.Assertions.assertTrue
import test_kulp.ScipTester

private class OrWitnessProblem(mk_objective: (LPAffExpr<Int>, LPAffExpr<Int>) -> LPAffExpr<Int>) :
    WitnessProblem(mk_objective) {

    override val witness = node { RIL.or(x, y) }
    val x_witness = node { RIL.or(x) }
    val empty_witness = node { RIL.or() }
}

class TestOrWitness : ScipTester() {

    @Test
    fun witnesses_true_both() {
        val prob = OrWitnessProblem { x, y -> x + y }
        val solution = solve_problem(prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(10.0, solution.value_of(prob.x))
        assertEquals(10.0, solution.value_of(prob.y))
        assertEquals(1.0, solution.value_of(prob.empty_witness))
        assertTrue(solution.value_of(prob.x_witness)!! >= 1)
        assertTrue(solution.value_of(prob.witness)!! >= 1)
    }

    @Test
    fun witnesses_true_one() {
        val prob = OrWitnessProblem { x, y -> y - x }
        val solution = solve_problem(prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(-10.0, solution.value_of(prob.x))
        assertEquals(10.0, solution.value_of(prob.y))
        assertEquals(1.0, solution.value_of(prob.empty_witness))
        assertTrue(solution.value_of(prob.x_witness)!! <= 0)
        assertTrue(solution.value_of(prob.witness)!! >= 0)
    }

    @Test
    fun witnesses_false() {
        val prob = OrWitnessProblem { x, y -> -x - y }
        val solution = solve_problem(prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(-10.0, solution.value_of(prob.x))
        assertEquals(-10.0, solution.value_of(prob.y))
        assertEquals(1.0, solution.value_of(prob.empty_witness))
        assertTrue(solution.value_of(prob.x_witness)!! <= 0)
        assertTrue(solution.value_of(prob.witness)!! <= 0)
    }

    @Test
    fun binds_feasible() {
        val prob = OrWitnessProblem { x, y -> -x - 2 * y }
        prob use { "test_bind_pin" { witness eq 1 } }
        val solution = solve_problem(prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(1.0, solution.value_of(prob.empty_witness))
        assertEquals(1.0, solution.value_of(prob.witness))
        // best satisfying values for or
        assertEquals(1.0, solution.value_of(prob.x))
        assertEquals(-10.0, solution.value_of(prob.y))
    }

    @Test
    fun binds_infeasible() {
        val prob = OrWitnessProblem { x, y -> x + y }
        prob use { "test_pin_x" { x le -1 } }
        prob use { "test_pin_y" { y le -1 } }
        prob use { "test_bind_pin" { witness eq 1 } }
        val solution = solve_problem(prob)
        assertEquals(LPSolutionStatus.Infeasible, solution.status())
    }
}
