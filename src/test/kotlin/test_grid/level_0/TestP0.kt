package test_grid.level_0

import grid_model.BAGP
import grid_model.Entity
import grid_model.adapters.lp.LPGridAdapter
import grid_model.dimension.D2
import grid_model.dimension.Vec.Companion.vec
import grid_model.predicate.BaseGridPredicate
import kotlin.test.Test
import kotlin.test.assertEquals
import kulp.LPSolutionStatus
import test_kulp.ScipTester

/**
 * Technically, this is an edge case which might be, ironically, harder to get right than e.g. a
 * simple 1x1 problem. But we work foundations up!
 */
private object EmptyProblem : TestGridProblem<D2>(D2) {

    // note: these are 1x1 bounds, not empty bounds!
    override val bounds = dim.vec(0, 0).to_origin_bb()

    override fun get_entity_set(): Set<Entity<D2>> = setOf()

    override fun get_setup_predicates(): Iterable<BaseGridPredicate> = listOf()

    override fun get_valuation_predicates(): Map<BAGP, Double> = mapOf()
}

class TestP0 : ScipTester() {

    @Test
    fun test_empty_problem() {
        val adapter = LPGridAdapter(EmptyProblem)
        val solution = solve_problem(adapter.lp_prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
    }
}
