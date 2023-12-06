package test_kulp.test_transforms

import kotlin.test.Test
import kotlin.test.assertEquals
import kulp.*
import kulp.transforms.IntMax
import kulp.transforms.IntMin
import kulp.variables.LPInteger
import model.LPName
import model.sn
import test_kulp.ScipTester

private class IntMaxMinProblem(val do_max: Boolean, val do_minimize: Boolean) : LPProblem() {

    val xs =
        listOf(
            LPInteger("x1".sn, null, 10),
            LPInteger("x2".sn, -10, 10),
            LPInteger("x3".sn, -10, null)
        )

    val x_pins =
        listOf(
            xs[0] eq -3.0 rooted "x1pin",
            xs[1] eq 5.0 rooted "x2pin",
            xs[2] eq 7.0 rooted "x3pin",
        )

    val yt = if (do_max) IntMax("y".sn, xs) else IntMin("min".sn, xs)

    override fun get_objective(): Pair<LPAffExpr<*>, LPObjectiveSense> {
        return if (do_minimize) Pair(yt, LPObjectiveSense.Minimize)
        else Pair(yt, LPObjectiveSense.Maximize)
    }

    override fun get_renderables(): List<LPRenderable> {
        return xs + x_pins + listOf(yt)
    }

    override val name: LPName = "IntMaxMinProblem".sn
}

class TestIntMaxMin : ScipTester() {

    @Test
    fun testMaxMinimize() {
        val prob = IntMaxMinProblem(do_max = true, do_minimize = true)
        val solution = solveProblem(prob)
        assertEquals(7.0, solution.objective_value())
        assertEquals(LPSolutionStatus.Optimal, solution.status())
    }

    @Test
    fun testMaxMaximize() {
        val prob = IntMaxMinProblem(do_max = true, do_minimize = false)
        val solution = solveProblem(prob)
        assertEquals(7.0, solution.objective_value())
        assertEquals(LPSolutionStatus.Optimal, solution.status())
    }

    @Test
    fun testMinMinimize() {
        val prob = IntMaxMinProblem(do_max = false, do_minimize = true)
        val solution = solveProblem(prob)
        assertEquals(-3.0, solution.objective_value())
        assertEquals(LPSolutionStatus.Optimal, solution.status())
    }

    @Test
    fun testMinMaximize() {
        val prob = IntMaxMinProblem(do_max = false, do_minimize = false)
        val solution = solveProblem(prob)
        assertEquals(-3.0, solution.objective_value())
        assertEquals(LPSolutionStatus.Optimal, solution.status())
    }
}
