package test_grid.level_1

import boolean_algebra.BooleanExpr.Companion.and
import grid_model.BEGP
import grid_model.Entity
import grid_model.GridProblem
import grid_model.entity
import grid_model.geom.D2
import grid_model.geom.Shape
import grid_model.geom.Shape.Companion.rect
import grid_model.geom.vec
import grid_model.plane.Onto
import grid_model.predicate.HasEntityOrMasked
import test_kulp.ScipTester
import test_kulp.assertObjective
import test_kulp.assertOptimal
import kotlin.test.Test

/**
 * Reproducing the classic example from the old python code: optimal creeper spawner.
 *
 * Details here:
 * https://old.reddit.com/r/technicalminecraft/comments/l8b5lj/proven_optimal_creeper_platform_carpeting_14/
 *
 * Features entity-entity dependencies.
 */
private object CreeperPlatform : GridProblem<D2>(D2) {

    // grid
    const val platform_size = 11

    override val arena = dim.vec(platform_size, platform_size)

    // entities
    private val carpet by entity(D2) { Onto { /* should be default 1x1 fermi*/} }
    private val solid by entity(D2) { Onto {} }
    private val no_spiders by entity(D2) { one_of(carpet, solid) { +rect(3, 3).shifted(-1, -1) } }

    // shapes
    private val cross =
        D2.run { Shape(setOf(vec(4, 5), vec(5, 5), vec(6, 5), vec(5, 4), vec(5, 6))) }

    // both carpets and spawnable squares, by luck, have the same mask
    private val default_mask
        get() =
            (cross.shifted(-5, -5) +
                    cross.shifted(-5, 5) +
                    cross.shifted(5, -5) +
                    cross.shifted(5, 5))
                .truncate(bounds)

    override fun get_entity_set(): Set<Entity<D2>> = setOf(carpet, no_spiders, solid)

    override fun generate_requirement_predicates(): BEGP<D2> =
        and(
            solid exactly_at cross,
            for_all_points { HasEntityOrMasked(no_spiders) },
        )

    override fun get_default_entity_mask(): Shape<D2> = default_mask

    override fun get_default_plane_mask(): Shape<D2> = default_mask

    override fun get_valuation_predicates(): Map<BEGP<D2>, Double> = val_entity_count(carpet, -1.0)
}

class TestCreeperSpawner : ScipTester() {

    @Test
    fun test_creeper_spawner() {
        CreeperPlatform.lp.run {
            val lp_sol = solve().apply { assertOptimal() }
            val g_sol = parse_solution(lp_sol)
            println(CreeperPlatform.sat_algebra)
            CreeperPlatform.bounds.forEach {
                println("ix: $it -> ${g_sol.get_entities(it)} -> ${g_sol.get_tiles(it, Onto)}")
            }
            lp_sol.run { assertObjective(-14.0) }
        }
    }
}
