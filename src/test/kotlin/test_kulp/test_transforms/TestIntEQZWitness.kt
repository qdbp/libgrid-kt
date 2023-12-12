package test_kulp.test_transforms

import kotlin.test.assertEquals
import kulp.LPAffExpr
import kulp.LPObjectiveSense
import kulp.LPSolutionStatus
import kulp.transforms.IntEQZWitness
import kulp.variables.LPInteger
import org.junit.jupiter.api.Test
import test_kulp.ScipTester
import test_kulp.TestLPProblem

private class ReifiedEqProblem(
    var lhs: LPAffExpr<Int>? = null,
    var rhs: LPAffExpr<Int>? = null,
    var objective: LPAffExpr<Int>? = null
) : TestLPProblem() {

    var z: IntEQZWitness? = null

    fun mk_z(): IntEQZWitness {
        z = node { "z" { IntEQZWitness(lhs!!, rhs!!) } }
        return z!!
    }

    override fun get_objective(): Pair<LPAffExpr<Int>, LPObjectiveSense> {
        return objective!! to LPObjectiveSense.Maximize
    }
}

class TestIntEQZWitness : ScipTester() {

    /**
     * Tests that we can have a feasible solution even if the equality variable is unsatisfiable, if
     * we do not require the equality variable to be true.
     */
    @Test
    fun testDisjointIntsUnconstrainedFeasible() {
        val prob = ReifiedEqProblem()
        val x = prob.root().bind("x") { LPInteger(0, 10) }
        val y = prob.root().bind("y") { LPInteger(15, 20) }
        prob.lhs = x
        prob.rhs = y
        prob.mk_z()
        prob.objective = prob.lhs!! + prob.rhs!!

        val solution = solve_problem(prob)

        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(0.0, solution.value_of(prob.z!!))
        assertEquals(10.0, solution.value_of(x))
        assertEquals(20.0, solution.value_of(y))
    }

    /** Conversely, if we force the equality variable to be true, we should get infeasible. */
    @Test
    fun testDisjointIntsConstrainedInfeasible() {
        val prob = ReifiedEqProblem()
        val x = prob.root().bind("x") { LPInteger(0, 10) }
        val y = prob.root().bind("y") { LPInteger(15, 20) }
        prob.lhs = x
        prob.rhs = y
        val z = prob.mk_z()
        prob.objective = prob.lhs!! + prob.rhs!!
        prob.root().bind("force_equal") { z eq 1 }

        val solution = solve_problem(prob)

        assertEquals(LPSolutionStatus.Infeasible, solution.status())
    }

    /** Here the equality pin should find a valid solution. */
    @Test
    fun testOverlappingIntsConstrainedFeasible() {
        val prob = ReifiedEqProblem()
        val x = prob.root().bind("x") { LPInteger(0, 10) }
        val y = prob.root().bind("y") { LPInteger(10, 20) }
        prob.lhs = x
        prob.rhs = y
        val z = prob.mk_z()
        prob.objective = y - x

        prob.root().bind("force_equal") { prob.z!! eq 1 }
        val solution = solve_problem(prob)

        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(1.0, solution.value_of(prob.z!!))
        assertEquals(10.0, solution.value_of(x))
        assertEquals(10.0, solution.value_of(y))
        assertEquals(0.0, solution.objective_value())
    }

    /** We repeat the test above, but use LPAffExpr<Int> instead of plain variables */
    @Test
    fun testFeasibleWithExprs() {
        val prob = ReifiedEqProblem()
        val x = prob.root().bind("x") { LPInteger(0, 5) }
        val y = prob.root().bind("y") { LPInteger(15, 20) }
        val w = prob.root().bind("w") { LPInteger(0, 10) }
        prob.lhs = x + w
        prob.rhs = y - w
        val z = prob.mk_z()
        prob.objective = -w

        prob.root().bind("force_equal") { z eq 1 }

        val solution = solve_problem(prob)

        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(1.0, solution.value_of(z))
        assertEquals(5.0, solution.value_of(x))
        assertEquals(15.0, solution.value_of(y))
        // we're forced to set w to 5
        assertEquals(5.0, solution.value_of(w))
        assertEquals(-5.0, solution.objective_value())
    }
}
