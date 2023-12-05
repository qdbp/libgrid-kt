package test_kulp.test_transforms

import kulp.*
import kulp.constraints.LP_EQ
import kulp.transforms.IntMax
import kulp.transforms.IntMin
import kulp.variables.LPInteger
import model.LPName
import model.sn
import test_kulp.ScipTester
import kotlin.test.Test
import kotlin.test.assertEquals

private class IntMaxMinProblem(val do_max: Boolean, val do_minimize: Boolean) : LPProblem() {

    val xs =
        listOf(
            LPInteger("x1".sn, null, 10),
            LPInteger("x2".sn, -10, 10),
            LPInteger("x3".sn, -10, null)
        )

    val x_pins =
        listOf(
            LP_EQ("x1pin".sn, xs[0], -3.0),
            LP_EQ("x2pin".sn, xs[1], 5.0),
            LP_EQ("x3pin".sn, xs[2], 7.0),
        )

    val yt = if (do_max) IntMax("max".sn, xs) else IntMin("min".sn, xs)

    override fun get_objective(): Pair<LPAffExpr<*>, LPObjectiveSense> {
        return if (do_minimize) Pair(yt, LPObjectiveSense.Minimize)
        else Pair(yt, LPObjectiveSense.Maximize)
    }

    override fun get_renderables(): List<LPRenderable> {
        return xs + x_pins + listOf(yt)
    }

    override val name: LPName = "IntMaxMinProblem".sn
}

class TestIntMaxMin: ScipTester() {

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
    fun testMinMinimizd() {
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
