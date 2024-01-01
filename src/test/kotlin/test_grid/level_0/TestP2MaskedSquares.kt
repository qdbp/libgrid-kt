package test_grid.level_0

import boolean_algebra.BooleanExpr
import grid_model.BEGP
import grid_model.Entity
import grid_model.Shape
import grid_model.dimension.D2
import grid_model.dimension.Vec.Companion.vec
import grid_model.entity
import grid_model.planes.Onto
import grid_model.planes.Plane
import grid_model.shapes.FreeShape.Companion.free
import grid_model.shapes.RectD
import range
import test_kulp.ScipTester
import test_kulp.assertObjective
import then
import times
import kotlin.test.Test

/** Test entity and plane masking in a toy example. */
private data class P2Problem(
    val mask_entities: List<Pair<Int, Int>> = listOf(),
    val mask_onto: List<Pair<Int, Int>> = listOf()
) : TestGridProblem<D2>(D2) {

    val Sq2x2 = entity(D2, "Sq2x2") { Onto { RectD.rect(2, 2).fermi } }

    // 10x10 grid
    override val bounds = dim.vec(9, 9).to_origin_bb()

    override fun get_entity_set(): Set<Entity<D2>> = setOf(Sq2x2)

    val static_conditions: MutableList<BEGP> = mutableListOf()

    override fun generate_requirement_predicates(): BEGP = BooleanExpr.and(static_conditions)

    override fun get_valuation_predicates(): Map<BEGP, Double> = val_entity_count(Sq2x2, 1.0)

    override fun generate_entity_mask(): Shape<D2> = free(mask_entities.map { it.toList() })

    override fun generate_plane_mask(plane: Plane): Shape<D2> =
        require(plane is Onto) then free(mask_onto.map { it.toList() })
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
