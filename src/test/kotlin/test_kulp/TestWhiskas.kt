package test_kulp

import com.google.ortools.Loader
import com.google.ortools.linearsolver.MPSolver
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kulp.*
import kulp.adapters.ORToolsAdapter
import kulp.constraints.LP_EQ
import kulp.constraints.LP_LEQ
import kulp.variables.LPNonnegativeReal
import kulp.variables.LPVariable
import model.sn
import org.junit.jupiter.api.Test

// implementing the example from pulp at
// https://coin-or.github.io/pulp/CaseStudies/a_blending_problem.html
private object WhiskasProblem : LPProblem() {

    override val name = "WhiskasProblem".sn

    // map of nutrient to (map of food to amount of nutrient per 1g)
    private val nutritional_provision =
        mapOf(
                "protein" to
                    mapOf(
                        "chicken" to 0.100,
                        "beef" to 0.200,
                        "mutton" to 0.150,
                        "rice" to 0.000,
                        "wheat" to 0.040,
                        "gel" to 0.000
                    ),
                "fat" to
                    mapOf(
                        "chicken" to 0.080,
                        "beef" to 0.100,
                        "mutton" to 0.110,
                        "rice" to 0.010,
                        "wheat" to 0.010,
                        "gel" to 0.000
                    ),
                "fibre" to
                    mapOf(
                        "chicken" to 0.001,
                        "beef" to 0.005,
                        "mutton" to 0.003,
                        "rice" to 0.100,
                        "wheat" to 0.150,
                        "gel" to 0.000
                    ),
                "salt" to
                    mapOf(
                        "chicken" to 0.002,
                        "beef" to 0.005,
                        "mutton" to 0.007,
                        "rice" to 0.002,
                        "wheat" to 0.008,
                        "gel" to 0.000
                    )
            )
            .mapKeys { it.key.sn }
            .mapValues { it.value.mapKeys { it.key.sn } }

    private val prices =
        mapOf(
                "chicken" to 0.013,
                "beef" to 0.008,
                "mutton" to 0.010,
                "rice" to 0.002,
                "wheat" to 0.005,
                "gel" to 0.001
            )
            .mapKeys { it.key.sn }

    private val max_nutrients = mapOf("fibre".sn to 2.00, "salt".sn to 0.40)
    private val min_nutrients = mapOf("protein".sn to 8.00, "fat".sn to 6.00)

    override fun get_objective(): Pair<LPExprLike, LPObjectiveSense> {
        val variables = whiskas_variables()
        val objective = variables.associateWith { prices[it.name]!! }.lp_dot()
        return Pair(objective, LPObjectiveSense.Minimize)
    }

    override fun get_renderables(): List<LPRenderable> {
        val renderables = mutableListOf<LPRenderable>()

        val variables = whiskas_variables()
        renderables.addAll(variables)

        // total weight sums to 100g
        renderables.add(LP_EQ("sums_to_1".sn, variables.lp_sum(), 100.0))

        // for each of the nutrients, the total amount of that nutrient provided by the food
        for (nutrient in listOf("protein", "fat", "fibre", "salt").map { it.sn }) {
            val nutrient_map = nutritional_provision[nutrient]!!
            val total_nutrient = variables.associateWith { nutrient_map[it.name]!! }.lp_dot()
            renderables.add(
                LP_LEQ(
                    nutrient.refine("met"),
                    min_nutrients[nutrient] ?: 0.0,
                    total_nutrient
                )
            )
            renderables.add(
                LP_LEQ(
                    nutrient.refine("not_exceeded"),
                    total_nutrient,
                    max_nutrients[nutrient] ?: Double.POSITIVE_INFINITY
                )
            )
        }

        return renderables
    }

    fun whiskas_variables(): List<LPVariable> {
        return listOf(
            LPNonnegativeReal("chicken".sn),
            LPNonnegativeReal("beef".sn),
            LPNonnegativeReal("mutton".sn),
            LPNonnegativeReal("rice".sn),
            LPNonnegativeReal("wheat".sn),
            LPNonnegativeReal("gel".sn)
        )
    }
}

internal class TestWhiskas {
    @Test
    fun testWhiskas() {
        // solve the whiskas problem with or-tools
        val ctx = MipContext(1000.0)
        Loader.loadNativeLibraries()
        val solver = MPSolver.createSolver("SCIP")
        val solution =
            solver.run {
                val adapter = ORToolsAdapter(WhiskasProblem, ctx)
                adapter.init()
                return@run adapter.run_solver()
            }
        assertEquals(solution.objective_value(), 0.52, 1e-6)
        for ((ingredient, wanted) in
            mapOf(
                "chicken" to 0.0,
                "beef" to 60.0,
                "mutton" to 0.0,
                "rice" to 0.0,
                "wheat" to 0.0,
                "gel" to 40.0
            )) {
            val value = solution.value_of(ingredient.sn)
            assertNotNull(value)
            assertEquals(value, wanted, 1e-6)
        }
    }
}
