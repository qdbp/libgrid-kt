package test_kulp.test_transforms

import kotlin.test.Test
import kotlin.test.assertEquals
import kulp.*
import kulp.transforms.Max
import kulp.transforms.Min
import kulp.variables.LPInteger
import test_kulp.ScipTester
import test_kulp.TestProblem

private class IntMaxMinProblem(val do_max: Boolean, val do_minimize: Boolean) : TestProblem() {

    val x1 = node + { LPInteger(it, null, 10) } named "x1" requiring { it eq -3 named "x1_pin" }
    val x2 = node + { LPInteger(it, -10, 10) } named "x2" requiring { it eq 5 named "x2_pin" }
    val x3 = node + { LPInteger(it, -10, null) } named "x3" requiring { it eq 7 named "x3_pin" }

    val xs = listOf(x1, x2, x3)

    val yt = node grow { if (do_max) Max(it, xs) else Min(it, xs) } named "yt"

    override fun get_objective(): Pair<LPAffExpr<*>, LPObjectiveSense> {
        return if (do_minimize) Pair(yt, LPObjectiveSense.Minimize)
        else Pair(yt, LPObjectiveSense.Maximize)
    }
}

class TestMaxMin : ScipTester() {

    @Test
    fun testMaxMinimize() {
        val prob = IntMaxMinProblem(do_max = true, do_minimize = true)
        val solution = solve_problem(prob)
        assertEquals(7.0, solution.objective_value())
        assertEquals(LPSolutionStatus.Optimal, solution.status())
    }

    @Test
    fun testMaxMaximize() {
        val prob = IntMaxMinProblem(do_max = true, do_minimize = false)
        val solution = solve_problem(prob)
        assertEquals(7.0, solution.objective_value())
        assertEquals(LPSolutionStatus.Optimal, solution.status())
    }

    @Test
    fun testMinMinimize() {
        val prob = IntMaxMinProblem(do_max = false, do_minimize = true)
        val solution = solve_problem(prob)
        assertEquals(-3.0, solution.objective_value())
        assertEquals(LPSolutionStatus.Optimal, solution.status())
    }

    @Test
    fun testMinMaximize() {
        val prob = IntMaxMinProblem(do_max = false, do_minimize = false)
        val solution = solve_problem(prob)
        assertEquals(-3.0, solution.objective_value())
        assertEquals(LPSolutionStatus.Optimal, solution.status())
    }
}
