package test_kulp.test_transforms

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kulp.*
import kulp.variables.LPInteger
import test_kulp.ScipTester

private class IntClipTestProblem(
    val mk_objective: (LPAffExpr<*>) -> Pair<LPAffExpr<*>, LPObjectiveSense>,
    lb: Int?,
    ub: Int?,
    val pin_x: Int? = null
) : LPProblem() {

    val x = node grow { LPInteger(it, pin_x, pin_x) } named "x"
    val y = node grow x.int_clip(lb, ub) named "yt"

    override fun get_objective(): Pair<LPAffExpr<*>, LPObjectiveSense> = mk_objective(y)
}

class TestIntClip : ScipTester() {

    @Test
    fun testLbOnly() {
        val prob = IntClipTestProblem({ it to LPObjectiveSense.Minimize }, lb = -10, ub = null)
        val solution = solve_problem(prob)
        assertNull(prob.y.z_ub)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(1.0, solution.value_of(prob.y.z_lb!!.node))
        assertEquals(-10.0, solution.objective_value())
    }

    @Test
    fun testLbOnlyForcePinnedNotBound() {
        val prob =
            IntClipTestProblem({ it to LPObjectiveSense.Minimize }, lb = -10, ub = null, pin_x = 7)
        val solution = solve_problem(prob)
        assertNull(prob.y.z_ub)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(0.0, solution.value_of(prob.y.z_lb!!.node))
        assertEquals(7.0, solution.objective_value())
    }

    @Test
    fun testLbOnlyForcePinnedBound() {
        val prob =
            IntClipTestProblem(
                { it to LPObjectiveSense.Minimize },
                lb = -10,
                ub = null,
                pin_x = -15
            )
        val solution = solve_problem(prob)
        assertNull(prob.y.z_ub)
        assertEquals(1.0, solution.value_of(prob.y.z_lb!!.node))
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(-10.0, solution.objective_value())
    }

    @Test
    fun testUbOnly() {
        val prob = IntClipTestProblem({ it to LPObjectiveSense.Maximize }, lb = null, ub = 10)
        val solution = solve_problem(prob)
        assertEquals(solution.status(), LPSolutionStatus.Optimal)
        assertEquals(10.0, solution.objective_value())
    }

    @Test
    fun testUbOnlyForcePinnedBound() {
        val prob =
            IntClipTestProblem({ it to LPObjectiveSense.Minimize }, lb = null, ub = 10, pin_x = 20)
        val solution = solve_problem(prob)
        assert(solution.status() == LPSolutionStatus.Optimal)
        assertNull(prob.y.z_lb)
        assertEquals(1.0, solution.value_of(prob.y.z_ub!!.node))
        assertEquals(10.0, solution.objective_value())
    }

    @Test
    fun testUbOnlyForcePinnedNotBound() {
        val prob =
            IntClipTestProblem({ it to LPObjectiveSense.Minimize }, lb = null, ub = 10, pin_x = 7)
        val solution = solve_problem(prob)
        assert(solution.status() == LPSolutionStatus.Optimal)
        assertNull(prob.y.z_lb)
        assertEquals(0.0, solution.value_of(prob.y.z_ub!!.node))
        assertEquals(7.0, solution.objective_value())
    }

    @Test
    fun testUbLBMaximize() {
        val prob = IntClipTestProblem({ it to LPObjectiveSense.Maximize }, lb = -10, ub = 10)
        val solution = solve_problem(prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(0.0, solution.value_of(prob.y.z_lb!!.node))
        assertEquals(1.0, solution.value_of(prob.y.z_ub!!.node))
        assertEquals(10.0, solution.objective_value())
    }

    @Test
    fun testUbLBMinimize() {
        val prob = IntClipTestProblem({ it to LPObjectiveSense.Minimize }, lb = -10, ub = 10)
        val solution = solve_problem(prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(1.0, solution.value_of(prob.y.z_lb!!.node))
        assertEquals(0.0, solution.value_of(prob.y.z_ub!!.node))
        assertEquals(-10.0, solution.objective_value())
    }

    @Test
    fun testUbLBMinimizeForcePinned() {
        val prob =
            IntClipTestProblem({ it to LPObjectiveSense.Minimize }, lb = -5, ub = 5, pin_x = -20)
        val solution = solve_problem(prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(solution.value_of(prob.y.z_lb!!.node), 1.0)
        assertEquals(solution.value_of(prob.y.z_ub!!.node), 0.0)
        assertEquals(solution.objective_value(), -5.0)
    }

    @Test
    fun testUbLBMaximizeForcePinned() {
        val prob =
            IntClipTestProblem({ it to LPObjectiveSense.Maximize }, lb = -5, ub = 5, pin_x = -20)
        val solution = solve_problem(prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(solution.value_of(prob.y.z_lb!!.node), 1.0)
        assertEquals(solution.value_of(prob.y.z_ub!!.node), 0.0)
        assertEquals(solution.objective_value(), -5.0)
    }
}
