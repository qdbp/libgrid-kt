package test_grid.level_0

import boolean_algebra.BooleanExpr
import grid_model.BEGP
import grid_model.Entity
import grid_model.entity
import grid_model.geom.D2
import grid_model.geom.Shape
import grid_model.geom.Shape.Companion.rect
import grid_model.geom.ones
import grid_model.geom.to_vec
import grid_model.plane.Onto
import grid_model.plane.Plane
import range
import test_kulp.ScipTester
import test_kulp.assertObjective
import times
import kotlin.test.Test

/** Test entity and plane masking in a toy example. */
private data class P2Problem(
    val mask_entities: List<Pair<Int, Int>> = listOf(),
    val mask_onto: List<Pair<Int, Int>> = listOf()
) : TestGridProblem<D2>(D2) {

    val Sq2x2 = entity(D2, "Sq2x2") { Onto { shape = rect(2, 2) } }

    // 10x10 grid
    override val arena = dim.ones(10)

    override fun get_entity_set(): Set<Entity<D2>> = setOf(Sq2x2)

    val static_conditions: MutableList<BEGP<D2>> = mutableListOf()

    override fun generate_requirement_predicates(): BEGP<D2> = BooleanExpr.and(static_conditions)

    override fun get_valuation_predicates(): Map<BEGP<D2>, Double> = val_entity_count(Sq2x2, 1.0)

    override fun get_entity_mask() = mapOf(Sq2x2 to Shape(mask_entities.map { it.to_vec() }, D2))

    override fun generate_tile_mask(): Map<Plane, Shape<D2>> =
        mapOf(Onto to Shape(mask_onto.map { it.to_vec() }, D2))
}

class TestP2MaskedSquares : ScipTester() {

    val corners = listOf(0 to 0, 0 to 9, 9 to 0, 9 to 9)
    val dots = range(1, 10, 2) * range(1, 10, 2)

    @Test fun sanity_check_unmasked() = P2Problem().lp.solve().run { assertObjective(25.0) }

    @Test
    fun mask_entities_corners() {
        // only lose one square: the one we can't place in the bottom left.
        // the other corners were never entity squares anyway!
        P2Problem(mask_entities = corners).lp.solve().run { assertObjective(24.0) }
    }

    @Test
    fun mask_entities_odd_dots() {
        P2Problem(mask_entities = dots).lp.solve().run { assertObjective(25.0) }
    }

    @Test
    fun mask_tiles_corners() {
        // masking tiles should actually lose us 2 squares, since we actually lose "physical" space,
        // and not just an abstract entity placemenet tile
        P2Problem(mask_onto = corners).lp.solve().run { assertObjective(23.0) }
    }

    @Test
    fun mask_tiles_odd_dots() {
        // unlike with entities, masking the checkerboard makes us unable to place anything
        P2Problem(mask_onto = dots).lp.solve().run { assertObjective(0.0) }
    }
}
