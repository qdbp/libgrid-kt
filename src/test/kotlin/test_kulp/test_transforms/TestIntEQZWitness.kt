package test_kulp.test_transforms

import kotlin.test.assertEquals
import kulp.*
import kulp.transforms.IntEQZWitness
import kulp.variables.BaseLPInteger
import kulp.variables.LPInteger
import org.junit.jupiter.api.Test
import test_kulp.ScipTester
import test_kulp.TestProblem

private class ReifiedEqProblem(
    var lhs: LPAffExpr<Int>? = null,
    var rhs: LPAffExpr<Int>? = null,
    var objective: LPAffExpr<Int>? = null
) : TestProblem() {

    var z: IntEQZWitness? = null

    fun mk_z(): IntEQZWitness {
        z = node grow { IntEQZWitness(it, lhs!!, rhs!!) } named "z"
        node.dump_full_tree_dfs().forEach { println(it) }
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
        val x = prob.root() grow { LPInteger(it, 0, 10) } named "x"
        val y = prob.root() grow { LPInteger(it, 15, 20) } named "y"
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
        val x = prob.root() grow { LPInteger(it, 0, 10) } named "x"
        val y = prob.root() grow { LPInteger(it, 15, 20) } named "y"
        prob.lhs = x
        prob.rhs = y
        val z = prob.mk_z()
        prob.objective = prob.lhs!! + prob.rhs!!
        prob.root() += z eq 1 named "force_equal"

        val solution = solve_problem(prob)

        assertEquals(LPSolutionStatus.Infeasible, solution.status())
    }

    /** Here the equality pin should find a valid solution. */
    @Test
    fun testOverlappingIntsConstrainedFeasible() {
        val prob = ReifiedEqProblem()
        val x = prob.root() grow { LPInteger(it, 0, 10) } named "x"
        val y = prob.root() grow { LPInteger(it, 10, 20) } named "y"
        prob.lhs = x
        prob.rhs = y
        val z = prob.mk_z()
        prob.objective = y - x

        prob.root() += prob.z!! eq 1 named "force_equal"
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
        val x = prob.root() grow { LPInteger(it, 0, 5) } named "x"
        val y = prob.root() grow { LPInteger(it, 15, 20) } named "y"
        val w = prob.root() grow { LPInteger(it, 0, 10) } named "w"
        prob.lhs = x + w
        prob.rhs = y - w
        val z = prob.mk_z()
        prob.objective = -w

        prob.root() += z eq 1 named "force_equal"

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
