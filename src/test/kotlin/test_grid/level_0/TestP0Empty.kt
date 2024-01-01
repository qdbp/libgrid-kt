package test_grid.level_0

import boolean_algebra.True
import grid_model.BEGP
import grid_model.Entity
import grid_model.dimension.D2
import grid_model.dimension.Vec.Companion.vec
import kotlin.test.Test
import test_kulp.ScipTester
import test_kulp.assertOptimal

/**
 * Technically, this is an edge case which might be, ironically, harder to get right than e.g. a
 * simple 1x1 problem. But we work foundations up!
 */
private object EmptyProblem : TestGridProblem<D2>(D2) {

    // note: these are 1x1 bounds, not empty bounds!
    override val bounds = dim.vec(0, 0).to_origin_bb()

    override fun get_entity_set(): Set<Entity<D2>> = setOf()

    override fun generate_requirement_predicates(): BEGP = True

    override fun get_valuation_predicates(): Map<BEGP, Double> = mapOf()
}

class TestP0Empty : ScipTester() {

    @Test
    fun test_empty_problem() = EmptyProblem.lp.solve().run { assertOptimal() }
}
