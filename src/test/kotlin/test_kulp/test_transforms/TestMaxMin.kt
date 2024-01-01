package test_kulp.test_transforms

import kotlin.test.Test
import kotlin.test.assertEquals
import kulp.LPAffExpr
import kulp.LPObjectiveSense
import kulp.LPSolutionStatus
import kulp.transforms.Max
import kulp.transforms.Min
import kulp.variables.LPInteger
import test_kulp.ScipTester
import test_kulp.TestLPProblem

private class IntMaxMinProblem(val do_max: Boolean, val do_minimize: Boolean) : TestLPProblem() {

    val x1 = node.bind("x1") { LPInteger(null, 10).requiring("x1_pin") { it eq -3 } }
    val x2 = node.bind("x2") { LPInteger(-10, 10).requiring("x2_pin") { it eq 5 } }
    val x3 = node.bind("x3") { LPInteger(-10, null).requiring("x3_pin") { it eq 7 } }

    val xs = listOf(x1, x2, x3)

    val yt = node.bind("yt") { if (do_max) Max(xs) else Min(xs) }

    override fun get_objective(): Pair<LPAffExpr<*>, LPObjectiveSense> {
        return if (do_minimize) Pair(yt, LPObjectiveSense.Minimize)
        else Pair(yt, LPObjectiveSense.Maximize)
    }
}

class TestMaxMin : ScipTester() {

    @Test
    fun testMaxMinimize() {
        val prob = IntMaxMinProblem(do_max = true, do_minimize = true)
        val solution = prob.solve()
        assertEquals(7.0, solution.objective_value())
        assertEquals(LPSolutionStatus.Optimal, solution.status())
    }

    @Test
    fun testMaxMaximize() {
        val prob = IntMaxMinProblem(do_max = true, do_minimize = false)
        val solution = prob.solve()
        assertEquals(7.0, solution.objective_value())
        assertEquals(LPSolutionStatus.Optimal, solution.status())
    }

    @Test
    fun testMinMinimize() {
        val prob = IntMaxMinProblem(do_max = false, do_minimize = true)
        val solution = prob.solve()
        assertEquals(-3.0, solution.objective_value())
        assertEquals(LPSolutionStatus.Optimal, solution.status())
    }

    @Test
    fun testMinMaximize() {
        val prob = IntMaxMinProblem(do_max = false, do_minimize = false)
        val solution = prob.solve()
        assertEquals(-3.0, solution.objective_value())
        assertEquals(LPSolutionStatus.Optimal, solution.status())
    }
}
