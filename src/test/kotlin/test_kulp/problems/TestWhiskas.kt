package test_kulp.problems

import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kulp.*
import kulp.constraints.LP_BND
import kulp.variables.LPNonnegativeReal
import org.junit.jupiter.api.Test
import test_kulp.ScipTester

// implementing the example from pulp at
// https://coin-or.github.io/pulp/CaseStudies/a_blending_problem.html
private object WhiskasProblem : LPProblem() {

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

    private val prices =
        mapOf(
            "chicken" to 0.013,
            "beef" to 0.008,
            "mutton" to 0.010,
            "rice" to 0.002,
            "wheat" to 0.005,
            "gel" to 0.001
        )

    val nutrient_vars =
        prices.keys.associateWith { nutrient -> node grow { LPNonnegativeReal(it) } named nutrient }

    private val max_nutrients = mapOf("fibre" to 2.00, "salt" to 0.40)
    private val min_nutrients = mapOf("protein" to 8.00, "fat" to 6.00)

    init {
        // total weight sums to 100g
        node += nutrient_vars.values.lp_sum() eq 100.0 named "total_weight"

        // for each of the nutrients, the total amount of that nutrient provided by the food
        for (nutrient in listOf("protein", "fat", "fibre", "salt")) {
            val nutrient_map = nutritional_provision[nutrient]!!
            val total_nutrient =
                nutrient_vars.map { (name, lpvar) -> nutrient_map[name]!! * lpvar }.lp_sum()
            node grow
                {
                    LP_BND(it, total_nutrient, min_nutrients[nutrient], max_nutrients[nutrient])
                } named
                "${nutrient}_in_range"
        }
    }

    override fun get_objective(): Pair<LPAffExpr<*>, LPObjectiveSense> {
        return Pair(
            prices.entries.map { (food, price) -> price * nutrient_vars[food]!! }.lp_sum(),
            LPObjectiveSense.Minimize
        )
    }
}

internal class TestWhiskas : ScipTester() {
    @Test
    fun testWhiskas() {
        val solution = solve_problem(WhiskasProblem)
        assertEquals(0.52, solution.objective_value(), 1e-6)
        for ((ingredient, wanted) in
            mapOf(
                "chicken" to 0.0,
                "beef" to 60.0,
                "mutton" to 0.0,
                "rice" to 0.0,
                "wheat" to 0.0,
                "gel" to 40.0
            )) {
            val value = solution.value_of(WhiskasProblem.nutrient_vars[ingredient]!!)
            assertNotNull(value)
            assertEquals(wanted, value, 1e-6)
        }
    }
}
