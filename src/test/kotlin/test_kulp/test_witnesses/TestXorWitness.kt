package test_kulp.test_witnesses

import kotlin.test.Test
import kotlin.test.assertEquals
import kulp.LPAffExpr
import kulp.LPSolutionStatus
import kulp.times
import kulp.transforms.ril.RIL
import kulp.use
import test_kulp.ScipTester

private class XorWitnessProblem(mk_objective: (LPAffExpr<Int>, LPAffExpr<Int>) -> LPAffExpr<Int>) :
    WitnessProblem(mk_objective) {
    override val witness = node { RIL.xor(x, y) }
}

class TestXorWitness : ScipTester() {

    @Test
    fun witnesses_false() {
        val prob = XorWitnessProblem { x, y -> x + y }
        val solution = solve_problem(prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(10.0, solution.value_of(prob.x))
        assertEquals(10.0, solution.value_of(prob.y))
        assertEquals(0.0, solution.value_of(prob.witness))
    }

    @Test
    fun witnesses_true() {
        val prob = XorWitnessProblem { x, y -> x - y }
        val solution = solve_problem(prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(10.0, solution.value_of(prob.x))
        assertEquals(-10.0, solution.value_of(prob.y))
        assertEquals(1.0, solution.value_of(prob.witness))
    }

    @Test
    fun binds_feasible() {
        val prob = XorWitnessProblem { x, y -> x + 2 * y }
        prob use { "test_bind_pin" { witness eq 1 } }
        val solution = solve_problem(prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(1.0, solution.value_of(prob.witness))
        assertEquals(0.0, solution.value_of(prob.x))
        assertEquals(10.0, solution.value_of(prob.y))
    }

    @Test
    fun binds_infeasible() {
        val prob = XorWitnessProblem { x, y -> x + 2 * y }
        prob use { "test_pin_x" { x ge 1 } }
        prob use { "test_pin_y" { y ge 1 } }
        prob use { "test_bind_pin" { witness eq 1 } }
        val solution = solve_problem(prob)
        assertEquals(LPSolutionStatus.Infeasible, solution.status())
    }
}
