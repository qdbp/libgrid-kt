package test_grid.level_0

import boolean_algebra.BooleanExpr.Companion.and
import grid_model.BEGP
import grid_model.Entity
import grid_model.entity
import grid_model.geom.D2
import grid_model.geom.Shape.Companion.rect
import grid_model.geom.ones
import grid_model.plane.Onto
import test_kulp.ScipTester
import test_kulp.assertInfeasible
import test_kulp.assertObjective
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A tiny grid with 1x1 squares or 2x1 squares.
 *
 * First nontrivial problem with more than one entity and actual constraints.
 */
private data class P1Problem(val size: Int, val do_1x1: Boolean, val do_2x1: Boolean) :
    TestGridProblem<D2>(D2) {

    val Sq1x1 = entity(D2, "Sq1x1") { Onto { onto_taut() } }
    val Sq2x1 =
        entity(D2, "Sq2x2") {
            Onto {
                onto_push()
                shape = rect(2, 1)
            }
        }

    override val arena = dim.ones(size)

    override fun get_entity_set(): Set<Entity<D2>> {
        val out = mutableSetOf<Entity<D2>>()
        if (do_1x1) out += Sq1x1
        if (do_2x1) out += Sq2x1
        return out
    }

    val static_conditions: MutableList<BEGP<D2>> = mutableListOf()

    override fun generate_requirement_predicates() = and(static_conditions)

    override fun get_valuation_predicates(): Map<BEGP<D2>, Double> {
        val out = mutableMapOf<BEGP<D2>, Double>()
        if (do_1x1) out += val_entity_count(Sq1x1, 1.0)
        if (do_2x1) out += val_entity_count(Sq2x1, 3.0)
        return out
    }
}

class TestP1TinySquares : ScipTester() {

    @Test
    fun test_1x1_sq1_only() {
        val gp = P1Problem(1, do_1x1 = true, do_2x1 = false)
        assertEquals(1, gp.bounds.size)
        // one 1x1 square
        gp.lp.run {
            val lp_sol = solve().apply { assertObjective(1.0) }
            val g_sol = parse_solution(lp_sol)
            gp.bounds.forEach {
                println("ix: $it -> ${g_sol.get_entities(it)} -> ${g_sol.get_tiles(it, Onto)}")
            }
        }
        // gp.lp.solve().run { assertObjective(1.0) }
    }

    @Test
    fun test_1x1_sq2_only() {
        P1Problem(1, do_1x1 = false, do_2x1 = true).lp.solve().run { assertObjective(0.0) }
    }

    @Test
    fun test_1x1_sq1_sq2() {
        P1Problem(1, do_1x1 = true, do_2x1 = true).lp.solve().run { assertObjective(1.0) }
    }

    @Test
    fun test_2x2_sq1_only() {
        val gp = P1Problem(2, do_1x1 = true, do_2x1 = false)
        assertEquals(4, gp.bounds.size)
        // four 1x1 squares
        gp.lp.solve().run { assertObjective(4.0) }
    }

    @Test
    fun test_2x2_sq2_only() {
        val gp = P1Problem(2, do_1x1 = false, do_2x1 = true)
        assertEquals(4, gp.bounds.size)
        gp.lp.run {
            val lp_sol = solve()
            val g_sol = parse_solution(lp_sol)
            println(gp.sat_algebra)
            gp.bounds.forEach {
                println("ix: $it -> ${g_sol.get_entities(it)} ->  ${g_sol.get_tiles(it, Onto)}")
            }
            lp_sol.run { assertObjective(6.0) }
        }
        // gp.lp.solve().run { assertObjective(6.0) }
    }

    @Test
    fun test_2x2_sq1_sq2() {
        // should still only use 2 2x1 squares
        P1Problem(2, do_1x1 = true, do_2x1 = true).lp.solve().run { assertObjective(6.0) }
    }

    @Test
    fun test_2x2_sq1_sq2_requiring_at_least_one_sq1() {
        P1Problem(2, do_1x1 = true, do_2x1 = true)
            .apply {
                // check we're idempotent here just for fun
                static_conditions += req_entity_count(Sq1x1, min = 1)
                static_conditions += req_entity_count(Sq1x1, min = 1)
                static_conditions += req_entity_count(Sq1x1, min = 1)
                static_conditions += req_entity_count(Sq1x1, min = 1)
                static_conditions += req_entity_count(Sq1x1, min = 1)
            }
            .lp
            .solve()
            // two 1x1 squares and one 2x1 square
            .run { assertObjective(5.0) }
    }

    @Test
    fun test_2x2_sq1_sq2_infeasible() {
        P1Problem(2, do_1x1 = true, do_2x1 = true)
            .apply { static_conditions += req_entity_count(Sq2x1, min = 3) }
            .lp
            .solve()
            .run { assertInfeasible() }
    }
}
