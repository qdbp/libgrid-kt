package test_kulp.problems

import com.google.ortools.Loader
import com.google.ortools.linearsolver.MPSolver
import kulp.*
import kulp.adapters.ORToolsAdapter
import kulp.aggregates.LPOneOfN
import kulp.constraints.LP_LEQ
import model.SegName
import model.sn
import kotlin.test.Test

/**
 * Like the regular discrete knapsack, except we are also given the choice of one of three
 * objects for each object slot.
 *
 * This tests `LPOneOfN`
 */
private object OneOfKnapsackProblem : LPProblem() {
    override val name: SegName = "OneOfKnapsackProblem".sn

    const val weight_limit: Double = 100.0
    const val num_slots = 5
    const val choices_per_slot = 3

    /** seven item slots of three choices per slot
     * Each slot also has a "null item" to signify not choosing it */
    val item_values = List(num_slots) { List(choices_per_slot) {(0..100).random()} + listOf(0) }
    val item_weights = List(num_slots) { List(choices_per_slot) {(0..100).random()} + listOf(0) }

    val item_selectors = List(num_slots) {
        LPOneOfN("selector".sn.refine(it), choices_per_slot + 1)
    }

    val chosen_weights = item_selectors.zip(item_values).map { pair -> pair.first.lp_dot(pair.second) }
    val chosen_values = item_selectors.zip(item_weights).map { pair -> pair.first.lp_dot(pair.second) }

    override fun get_objective(): Pair<LPExprLike, LPObjectiveSense> {
        return Pair(chosen_values.lp_sum(), LPObjectiveSense.Maximize)
    }

    override fun get_renderables(): List<LPRenderable> {
        return item_selectors + listOf(
            LP_LEQ("weight_limit".sn, chosen_weights.lp_sum(), weight_limit),
        )
    }

}

class TestOneOfKnapsack {

    @Test
    fun test() {
        val ctx = MipContext()
        Loader.loadNativeLibraries()
        val solver = MPSolver.createSolver("SCIP")
        val solution =
            solver.run {
                val adapter = ORToolsAdapter(OneOfKnapsackProblem, ctx)
                adapter.init()
                return@run adapter.run_solver()
            }
        assert(solution.status() == LPSolutionStatus.Optimal)
        assert(solution.objective_value() >= 51)
    }
}
