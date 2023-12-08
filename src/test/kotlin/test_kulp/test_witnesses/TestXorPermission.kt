package test_kulp.test_witnesses

import kotlin.test.Test
import kotlin.test.assertEquals
import kulp.*
import kulp.transforms.LogicWitness
import kulp.variables.BaseLPInteger
import kulp.variables.LPInteger
import test_kulp.ScipTester
import test_kulp.TestProblem

private class XorWitnessProblem(
    val mk_objective: (BaseLPInteger, BaseLPInteger) -> LPAffExpr<Int>
) : TestProblem() {

    val x = node grow { LPInteger(it, -10, 10) } named "x"
    val y = node grow { LPInteger(it, -10, 10) } named "y"

    val witness = node + LogicWitness.xor(x, y) named "witness"

    override fun get_objective(): Pair<LPAffExpr<*>, LPObjectiveSense> =
        mk_objective(x, y) to LPObjectiveSense.Maximize
}

class TestXorPermission : ScipTester() {

    @Test
    fun witnesses_false() {
        val prob = XorWitnessProblem { x, y -> x + y }
        val solution = solve_problem(prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(10.0, solution.value_of(prob.x))
        assertEquals(10.0, solution.value_of(prob.y))
        assertEquals(0.0, solution.value_of(prob.witness.node))
    }

    @Test
    fun witnesses_true() {
        val prob = XorWitnessProblem { x, y -> x - y }
        val solution = solve_problem(prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(10.0, solution.value_of(prob.x))
        assertEquals(-10.0, solution.value_of(prob.y))
        assertEquals(1.0, solution.value_of(prob.witness.node))
    }

    @Test
    fun binds_feasible() {
        val prob = XorWitnessProblem { x, y -> x + 2 * y }
        prob.witness.node += prob.witness eq 1 named "test_bind_pin"
        val solution = solve_problem(prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(1.0, solution.value_of(prob.witness.node))
        assertEquals(0.0, solution.value_of(prob.x))
        assertEquals(10.0, solution.value_of(prob.y))
    }
}
