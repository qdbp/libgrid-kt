package test_grid.level_0

import grid_model.Entity
import grid_model.GridProblem
import grid_model.adapters.lp.LPGridAdapter
import grid_model.dimension.D2
import grid_model.dimension.Dim
import grid_model.dimension.Vec
import grid_model.dimension.Vec.Companion.vec
import grid_model.predicate.BaseGridPredicate
import kotlin.test.Test
import kotlin.test.assertEquals
import kulp.LPSolutionStatus
import test_kulp.ScipTester

abstract class TestGridProblem<D : Dim<D>>(dim: D) : GridProblem<D>(dim) {
    final override val name = this.javaClass.simpleName
}

/**
 * Technically, this is an edge case which might be, ironically, harder to get right than e.g. a
 * simple 1x1 problem. But we work foundations up! w
 */
object EmptyProblem : TestGridProblem<D2>(D2) {

    override val grid_size: Vec<D2> = dim.vec(0, 0)

    override fun get_entity_set(): Set<Entity> = setOf()

    override fun get_setup_predicates(): Iterable<BaseGridPredicate> = listOf()
}

class TestEmptyProblem : ScipTester() {

    @Test
    fun test_empty_problem() {
        val lp_prob = LPGridAdapter(EmptyProblem).compile()
        val solution = solve_problem(lp_prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
    }
}
