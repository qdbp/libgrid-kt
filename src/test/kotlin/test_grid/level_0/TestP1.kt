package test_grid.level_0

import grid_model.BAGP
import grid_model.Entity
import grid_model.adapters.lp.LPGridAdapter
import grid_model.dimension.D2
import grid_model.dimension.Vec.Companion.vec
import grid_model.entity
import grid_model.planes.Onto
import grid_model.predicate.BaseGridPredicate
import grid_model.shapes.Rect2
import kotlin.test.Test
import kotlin.test.assertEquals
import kulp.LPSolutionStatus
import test_kulp.ScipTester

/**
 * A tiny grid that can fit a 1x1 square or a 2x2 square.
 *
 * First nontrivial problem with more than one entity and actual constraints.
 */
private data class P1Problem(val size: Int, val do_1x1: Boolean, val do_2x2: Boolean) :
    TestGridProblem<D2>(D2) {

    companion object {
        private val Sq1x1 = entity(D2, "Sq1x1") { Onto { Rect2(1, 1).fermionic } }
        private val Sq2x2 = entity(D2, "Sq2x2") { Onto { Rect2(2, 2).fermionic } }
    }

    override val bounds = dim.vec(size - 1, size - 1).to_origin_bb()

    override fun get_entity_set(): Set<Entity<D2>> {
        val out = mutableSetOf<Entity<D2>>()
        if (do_1x1) out += Sq1x1
        if (do_2x2) out += Sq2x2
        return out
    }

    override fun get_setup_predicates(): Iterable<BaseGridPredicate> = listOf()

    override fun get_valuation_predicates(): Map<BAGP, Double> {
        val out = mutableMapOf<BAGP, Double>()
        if (do_1x1) out += entity_count_valuation(Sq1x1, 1.0)
        if (do_2x2) out += entity_count_valuation(Sq2x2, 5.0)
        return out
    }
}

class TestP1 : ScipTester() {

    @Test
    fun test_1x1_sq1_only() {
        val gp = P1Problem(1, do_1x1 = true, do_2x2 = false)
        assertEquals(1, gp.bounds.size)

        val adapter = LPGridAdapter(gp)
        val solution = solve_problem(adapter.lp_prob)

        assertEquals(LPSolutionStatus.Optimal, solution.status())
        // one 1x1 square
        assertEquals(1.0, solution.objective_value())
    }

    @Test
    fun test_1x1_sq2_only() {
        val gp = P1Problem(1, do_1x1 = false, do_2x2 = true)
        assertEquals(1, gp.bounds.size)

        val adapter = LPGridAdapter(gp)
        val solution = solve_problem(adapter.lp_prob)

        assertEquals(LPSolutionStatus.Optimal, solution.status())
        // one 1x1 square
        assertEquals(0.0, solution.objective_value())
    }

    @Test
    fun test_1x1_sq1_sq2() {
        val gp = P1Problem(1, do_1x1 = true, do_2x2 = true)
        assertEquals(1, gp.bounds.size)

        val adapter = LPGridAdapter(gp)
        val solution = solve_problem(adapter.lp_prob)

        assertEquals(LPSolutionStatus.Optimal, solution.status())
        // one 1x1 square
        assertEquals(1.0, solution.objective_value())
    }
}
