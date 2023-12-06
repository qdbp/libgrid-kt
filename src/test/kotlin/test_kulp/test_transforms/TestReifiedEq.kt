package test_kulp.test_transforms

import kotlin.test.assertEquals
import kulp.*
import kulp.LPConstraint
import kulp.constraints.LP_EQZ
import kulp.transforms.IntEQZWitness
import kulp.variables.LPInteger
import model.LPName
import model.sn
import org.junit.jupiter.api.Test
import test_kulp.ScipTester

private class ReifiedEqProblem(
    lhs: LPAffExpr<Int>,
    rhs: LPAffExpr<Int>,
    val objective: LPAffExpr<Int>,
    var constraints: MutableList<LPConstraint> = mutableListOf(),
    var extra_renderables: MutableList<LPRenderable> = mutableListOf(),
) : LPProblem() {

    val z = IntEQZWitness("z".sn, lhs, rhs)

    override fun get_objective(): Pair<LPAffExpr<Int>, LPObjectiveSense> {
        return objective to LPObjectiveSense.Maximize
    }

    override fun get_renderables(): List<LPRenderable> {
        return listOf(z) + constraints + extra_renderables
    }

    override val name: LPName = "ReifiedEqProblem".sn
}

class TestReifiedEq : ScipTester() {

    /**
     * Tests that we can have a feasible solution even if the equality variable is unsatisfiable, if
     * we do not require the equality variable to be true.
     */
    @Test
    fun testDisjointIntsUnconstrainedFeasible() {
        val x = LPInteger("x".sn, 0, 10)
        val y = LPInteger("y".sn, 15, 20)
        val objective = x + y

        val prob = ReifiedEqProblem(x, y, objective, extra_renderables = mutableListOf(x, y))

        val solution = solveProblem(prob)

        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(0.0, solution.value_of(prob.z.name))
        assertEquals(10.0, solution.value_of(x.name))
        assertEquals(20.0, solution.value_of(y.name))
    }

    /** Conversely, if we force the equality variable to be true, we should get infeasible. */
    @Test
    fun testDisjointIntsConstrainedInfeasible() {
        val x = LPInteger("x".sn, 0, 10)
        val y = LPInteger("y".sn, 15, 20)
        val objective = x + y

        val prob = ReifiedEqProblem(x, y, objective, extra_renderables = mutableListOf(x, y))
        prob.constraints.add(prob.z eq 1 rooted "force_equal")

        val solution = solveProblem(prob)

        assertEquals(LPSolutionStatus.Infeasible, solution.status())
    }

    /** Here the equality pin should find a valid solution. */
    @Test
    fun testOverlappingIntsConstrainedFeasible() {

        val x = LPInteger("x".sn, 0, 10)
        val y = LPInteger("y".sn, 10, 20)

        val objective = y - x

        val prob = ReifiedEqProblem(x, y, objective, extra_renderables = mutableListOf(x, y))
        prob.constraints.add(prob.z eq 1 rooted "force_equal")

        val solution = solveProblem(prob)

        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(1.0, solution.value_of(prob.z.name))
        assertEquals(10.0, solution.value_of(x.name))
        assertEquals(10.0, solution.value_of(y.name))
        assertEquals(0.0, solution.objective_value())
    }

    /** We repeat the test above, but use LPAffExpr<Int> instead of plain variables */
    @Test
    fun testFeasibleWithExprs() {

        val x = LPInteger("x".sn, 0, 5)
        val y = LPInteger("y".sn, 15, 20)
        val w = LPInteger("w".sn, 0, 10)

        val objective = - w

        val prob =
            ReifiedEqProblem(x + w, y - w, objective, extra_renderables = mutableListOf(x, y, w))

        prob.constraints.add(prob.z eq 1 rooted "force_equal")

        val solution = solveProblem(prob)

        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(1.0, solution.value_of(prob.z.name))
        assertEquals(5.0, solution.value_of(x.name))
        assertEquals(15.0, solution.value_of(y.name))
        // we're forced to set w to 5
        assertEquals(5.0, solution.value_of(w.name))
        assertEquals(-5.0, solution.objective_value())
    }
}
