package test_kulp.problems

import kotlin.test.Test
import kulp.LPObjective
import kulp.LPObjectiveSense
import kulp.LPProblem
import kulp.LPSolutionStatus
import kulp.aggregates.LPOneOfN
import mdspan.NDSpan
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
    val item_values: NDSpan<Int> =
        NDSpanImpl.full_by(shape) { ix -> if (ix[1] < choices_per_slot) (0..100).random() else 0 }
    val item_weights: NDSpan<Int> =
        NDSpanImpl.full_by(shape) { ix -> if (ix[1] < choices_per_slot) (0..100).random() else 0 }

    // test multidimensional contraints here
    val sel_arr = node {
        // constrain last dimension
        "selectors" {
            LPOneOfN(
                shape = listOf(num_slots, choices_per_slot + 1),
                constraint_subspace = listOf(1),
            )
        }
    }

    val total_weight = sel_arr.hadamard(item_weights) { a, b -> a * b }.lp_sum()
    val total_value = sel_arr.hadamard(item_values) { a, b -> a * b }.lp_sum()

    init {
        node.bind("weight_limit") { total_weight le weight_limit }
    }

    override fun get_objective(): LPObjective = total_value to LPObjectiveSense.Maximize
}

class TestOneOfKnapsack : ScipTester() {

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
