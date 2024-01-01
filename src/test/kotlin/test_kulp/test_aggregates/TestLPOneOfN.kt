package test_kulp.test_aggregates

import kulp.LPObjective
import kulp.LPObjectiveSense
import kulp.ProvenInfeasible
import kulp.aggregates.LPOneOfN
import kulp.expressions.PosBinary
import kulp.expressions.Zero
import kulp.lp_sum
import test_kulp.ScipTester
import test_kulp.TestLPProblem
import test_kulp.assertObjective
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

private class LPOneOfNTest(
    val shape: List<Int> = listOf(2, 3, 5),
    val constraint_subspace: List<Int> = listOf(2),
    val mask: Set<List<Int>> = setOf(),
) : TestLPProblem() {

    val xs by bind { LPOneOfN(shape, constraint_subspace = constraint_subspace, mask = mask) }

    override fun get_objective(): LPObjective = xs.lp_sum() to LPObjectiveSense.Maximize
}

class TestLPOneOfN : ScipTester() {

    @Test
    fun test_basic() {
        LPOneOfNTest().solve().run { assertObjective(6.0) }
    }

    @Test
    fun test_multidim_subspace() {
        LPOneOfNTest(constraint_subspace = listOf(0, 2)).solve().run { assertObjective(3.0) }
    }

    @Test
    fun test_masked_valid() {
        LPOneOfNTest(mask = setOf(listOf(0, 0, 0), listOf(1, 1, 1)))
            .apply {
                assertIs<Zero>(xs[0, 0, 0])
                assertIs<Zero>(xs[1, 1, 1])
                assertIs<PosBinary>(xs[0, 0, 1])
            }
            .solve()
            .run { assertObjective(6.0) }
    }

    @Test
    fun test_masked_invalid_raises() {
        assertFailsWith<ProvenInfeasible> {
            // we zero out the entirety of [_, 0, 0] which falls within the constraint subspace,
            // so we should raise infeasible since [0, 0, 0] + [1, 0, 0] can never sum to 1.
            LPOneOfNTest(
                    mask = setOf(listOf(0, 0, 0), listOf(1, 0, 0)),
                    constraint_subspace = listOf(0)
                )
                .solve()
        }
    }
}
