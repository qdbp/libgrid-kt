package test_kulp.test_ril

import kulp.LPAffExpr
import kulp.LPSolutionStatus
import kulp.times
import kulp.ril.RIL
import kulp.use
import test_kulp.ScipTester
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class AndRILProblem(mk_objective: (LPAffExpr<Int>, LPAffExpr<Int>) -> LPAffExpr<Int>) :
    RILProblem(mk_objective) {

    override val witness = node { RIL.and(x, y) }
    val x_witness = node { RIL.and(x) }
    val empty_witness = node { RIL.and() }
}

class TestRILAnd : ScipTester() {

    @Test
    fun witnesses_false() {
        val prob = AndRILProblem { x, y -> x + y }
        val solution = prob.solve()
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(10.0, solution.value_of(prob.x))
        assertEquals(10.0, solution.value_of(prob.y))
        assertEquals(1.0, solution.value_of(prob.empty_witness))
        assertTrue(solution.value_of(prob.x_witness)!! >= 1)
        assertTrue(solution.value_of(prob.witness)!! >= 1)
    }

    @Test
    fun witnesses_true_one() {
        val prob = AndRILProblem { x, y -> y - x }
        val solution = prob.solve()
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(-10.0, solution.value_of(prob.x))
        assertEquals(10.0, solution.value_of(prob.y))
        assertEquals(1.0, solution.value_of(prob.empty_witness))
        assertTrue(solution.value_of(prob.x_witness)!! <= 0)
        assertTrue(solution.value_of(prob.witness)!! <= 0)
    }

    @Test
    fun binds_feasible() {
        val prob = AndRILProblem { x, y -> -x - y }
        prob use { "test_bind_pin" { witness eq 1 } }
        val solution = prob.solve()
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(1.0, solution.value_of(prob.witness))
        assertEquals(1.0, solution.value_of(prob.empty_witness))
        // minimum satisfying values for and
        assertEquals(1.0, solution.value_of(prob.x))
        assertEquals(1.0, solution.value_of(prob.y))
    }

    @Test
    fun binds_infeasible() {
        val prob = AndRILProblem { x, y -> x + 2 * y }
        prob use { "test_pin_x" { x ge 1 } }
        prob use { "test_bind_pin_y" { y le -1 } }
        prob use { "test_bind_pin_wit" { witness eq 1 } }
        val solution = prob.solve()
        assertEquals(LPSolutionStatus.Infeasible, solution.status())
    }
}
