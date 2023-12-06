package test_kulp.problems

import kotlin.test.Test
import kulp.*
import kulp.aggregates.LPOneOfN
import model.LPName
import model.sn
import test_kulp.ScipTester

/**
 * Like the regular discrete knapsack, except we are also given the choice of one of three objects
 * for each object slot.
 *
 * This tests `LPOneOfN`
 */
private object OneOfKnapsackProblem : LPProblem() {
    override val name: LPName = "OneOfKnapsackProblem".sn

    const val weight_limit: Double = 100.0
    const val num_slots = 5
    const val choices_per_slot = 3

    /**
     * seven item slots of three choices per slot Each slot also has a "null item" to signify not
     * choosing it
     */
    val item_values = List(num_slots) { List(choices_per_slot) { (0..100).random() } + listOf(0) }
    val item_weights = List(num_slots) { List(choices_per_slot) { (0..100).random() } + listOf(0) }

    val item_selectors =
        List(num_slots) { LPOneOfN("selector".sn.refine(it), choices_per_slot + 1) }

    val chosen_weights =
        item_selectors.zip(item_values).map { pair -> pair.first.lp_dot(pair.second) }
    val chosen_values =
        item_selectors.zip(item_weights).map { pair -> pair.first.lp_dot(pair.second) }

    override fun get_objective(): Pair<LPAffExpr<*>, LPObjectiveSense> {
        return Pair(chosen_values.lp_sum(), LPObjectiveSense.Maximize)
    }

    override fun get_renderables(): List<LPRenderable> {
        return item_selectors + (chosen_weights.lp_sum() le weight_limit named "weight_limit".sn)
    }
}

class TestOneOfKnapsacku : ScipTester() {

    @Test
    fun test() {
        val solution = solveProblem(OneOfKnapsackProblem)
        assert(solution.status() == LPSolutionStatus.Optimal) {
            "status ${solution.status()} != Optimal"
        }
        assert(solution.objective_value() >= 51) {
            "objective value ${solution.objective_value()} < 51"
        }
    }
}
