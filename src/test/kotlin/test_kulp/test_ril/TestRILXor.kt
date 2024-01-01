package test_kulp.test_ril

import kotlin.test.Test
import kotlin.test.assertEquals
import kulp.LPAffExpr
import kulp.LPSolutionStatus
import kulp.times
import kulp.ril.RIL
import kulp.use
import test_kulp.ScipTester

private class XorRILProblem(mk_objective: (LPAffExpr<Int>, LPAffExpr<Int>) -> LPAffExpr<Int>) :
    RILProblem(mk_objective) {
    override val witness = node { RIL.xor(x, y) }
}

class TestRILXor : ScipTester() {

    @Test
    fun witnesses_false() {
        val prob = XorRILProblem { x, y -> x + y }
        val solution = prob.solve()
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(10.0, solution.value_of(prob.x))
        assertEquals(10.0, solution.value_of(prob.y))
        assertEquals(0.0, solution.value_of(prob.witness))
    }

    @Test
    fun witnesses_true() {
        val prob = XorRILProblem { x, y -> x - y }
        val solution = prob.solve()
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(10.0, solution.value_of(prob.x))
        assertEquals(-10.0, solution.value_of(prob.y))
        assertEquals(1.0, solution.value_of(prob.witness))
    }

    @Test
    fun binds_feasible() {
        val prob = XorRILProblem { x, y -> x + 2 * y }
        prob use { "test_bind_pin" { witness eq 1 } }
        val solution = prob.solve()
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(1.0, solution.value_of(prob.witness))
        assertEquals(0.0, solution.value_of(prob.x))
        assertEquals(10.0, solution.value_of(prob.y))
    }

    @Test
    fun binds_infeasible() {
        val prob = XorRILProblem { x, y -> x + 2 * y }
        prob use { "test_pin_x" { x ge 1 } }
        prob use { "test_pin_y" { y ge 1 } }
        prob use { "test_bind_pin" { witness eq 1 } }
        val solution = prob.solve()
        assertEquals(LPSolutionStatus.Infeasible, solution.status())
    }
}
