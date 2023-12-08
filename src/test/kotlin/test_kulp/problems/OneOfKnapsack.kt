package test_kulp.problems

import kotlin.test.Test
import kulp.*
import kulp.aggregates.LPOneOfN
import mdspan.NDSpanImpl
import mdspan.lp_sum
import test_kulp.ScipTester

/**
 * Like the regular discrete knapsack, except we are also given the choice of one of three objects
 * for each object slot.
 *
 * This tests `LPOneOfN`
 */
private object OneOfKnapsackProblem : LPProblem() {

    const val weight_limit: Double = 100.0
    const val num_slots = 5
    const val choices_per_slot = 3

    val shape = listOf(num_slots, choices_per_slot + 1)

    /**
     * seven item slots of three choices per slot Each slot also has a "null item" to signify not
     * choosing it
     */
    val item_values = NDSpanImpl.full_by(shape) { (0..100).random() }
    val item_weights = NDSpanImpl.full_by(shape) { (0..100).random() }

    // test multidimensional contraints here
    val selectors =
        node grow
            {
                // constrain last dimension
                LPOneOfN(
                    it,
                    shape = listOf(num_slots, choices_per_slot + 1),
                    constraint_subspace = listOf(1),
                )
            } named
            "selectors"

    val total_weight = selectors.hadamard(item_weights) { a, b -> a * b }.lp_sum()
    val total_value = selectors.hadamard(item_values) { a, b -> a * b }.lp_sum()

    init {
        node += (total_weight le weight_limit) named "weight_limit"
    }

    override fun get_objective(): LPObjective = null_objective
}

class TestOneOfKnapsacku : ScipTester() {

    @Test
    fun test() {
        val solution = solve_problem(OneOfKnapsackProblem)
        assert(solution.status() == LPSolutionStatus.Optimal) {
            "status ${solution.status()} != Optimal"
        }
        assert(solution.objective_value() >= 51) {
            "objective value ${solution.objective_value()} < 51"
        }
    }
}
