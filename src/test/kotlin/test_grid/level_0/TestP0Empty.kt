package test_grid.level_0

import boolean_algebra.True
import grid_model.BEGP
import grid_model.Entity
import grid_model.geom.D2
import grid_model.geom.vec
import test_kulp.ScipTester
import test_kulp.assertOptimal
import kotlin.test.Test

/**
 * Technically, this is an edge case which might be, ironically, harder to get right than e.g. a
 * simple 1x1 problem. But we work foundations up!
 */
private object EmptyProblem : TestGridProblem<D2>(D2) {

    // note: these are 1x1 bounds, not empty bounds!
    override val arena = dim.vec(1, 1)

    override fun get_entity_set(): Set<Entity<D2>> = setOf()

    override fun generate_requirement_predicates(): BEGP<D2> = True

    override fun get_valuation_predicates(): Map<BEGP<D2>, Double> = mapOf()
}

class TestP0Empty : ScipTester() {

    @Test fun test_empty_problem() = EmptyProblem.lp.solve().run { assertOptimal() }
}
