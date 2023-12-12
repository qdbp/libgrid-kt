package grid_model.adapters.lp

import boolean_algebra.*
import grid_model.*
import grid_model.adapters.lp.pc.lp_compile
import grid_model.dimension.Dim
import kulp.*
import kulp.aggregates.LPOneOfN
import kulp.expressions.IntAffExpr
import kulp.transforms.ril.RIL
import kulp.variables.LPBinary
import mdspan.ndindex

// TODO right now we hardcode LocatedPointPredicate, but in reality we need to be able to handle
//  a collection of predicates. The GridProblem will return
class LPGridAdapter<D : Dim<D>>(val gp: GridProblem<D>) {

    fun compile(): LPProblem {
        return object : LPProblem() {
            override fun get_objective(): LPObjective = null_objective

            val e_chart = node { lp_set_up_entities() }
            val t_chart = node { lp_set_up_tiles() }

            // TODO lp_set_up_potentials()
            // TODO lp_set_up_flows()

            val chart = GridLPChart(e_chart, gp.ptc, t_chart)

            val sat_al = gp.generate_satisfaction_algebra()
            val ril_constraint_expr = node {
                ril_compile_algebra(chart, sat_al).also { "is_sat" { it ge 1 } }
            }
            val ril_setup_expr = node {
                ril_compile_algebra(chart, gp.setup_predicates).also { "is_sat" { it ge 1 } }
            }
        }
    }

    context(NodeCtx)
    private fun lp_set_up_entities(): LPEntityChart {
        // entities are not mutually exclusive, so we add a free LPBinary for each (entity, ix)
        val chart_map = mutableMapOf<List<Int>, MutableMap<Entity, LPBinary>>()
        for (entity in gp.entities) {
            for (ndix in ndindex(gp.grid_size)) {
                "${entity.name}_${ndix.lp_name}" {
                    LPBinary().also { chart_map.getOrPut(ndix) { mutableMapOf() }[entity] = it }
                }
            }
        }
        return LPEntityChart(chart_map)
    }

    /** Sets up the fundamental backing LP variables of the problem */
    context(NodeCtx)
    private fun lp_set_up_tiles(): LPTileChart {
        // set up tile variables
        val tile_lp_map = mutableMapOf<Pair<List<Int>, Tile>, LPAffExpr<Int>>()
        for (plane in gp.ptc.all_planes()) {
            val tiles = gp.ptc.tiles_of(plane)
            // + 1, since we need to add a "no tile" option. By convention, this will be the
            // last lpvar in the one-of-n aggregate. No one outside this class should care.
            val one_of_n = "${plane}_tiles" {
                // constrained along last dim by default
                LPOneOfN(gp.grid_size + (tiles.size + 1))
            }

            add_to_tile_chart(tile_lp_map, tiles, gp.boundary_conditions(plane), one_of_n)
        }
        // TODO we can do better than !! eventually
        return LPTileChart { ndix, tile -> tile_lp_map[ndix to tile]!! }
    }

    /** Uses the boundary conditions to create the tile chart for a given plane. */
    private fun add_to_tile_chart(
        out_map: MutableMap<Pair<List<Int>, Tile>, LPAffExpr<Int>>,
        tiles: List<Tile>,
        bcs_by_dim: List<BoundaryCondition>,
        one_of_n: LPOneOfN
    ): LPTileChart {

        // base map for in-rang3
        val base_map: MutableMap<List<Int>, MutableMap<Tile, LPAffExpr<Int>>> = mutableMapOf()

        // note: no + 1 here, since we do not add the "no tile" binary option to the chart
        for (ndix in ndindex(gp.grid_size + tiles.size)) {
            base_map.getOrPut(ndix) { mutableMapOf() }[tiles[ndix.last()]] = one_of_n.arr[ndix]
        }

        return object : LPTileChart {
            override fun get(grid_ndix: List<Int>, tile: Tile): LPAffExpr<Int> {
                val reduced_ndix = mutableListOf<Int>()
                grid_ndix.forEachIndexed { dim, dx ->
                    if (dim in 0 until gp.grid_size[dx]) {
                        reduced_ndix.add(dx)
                    } else {
                        when (bcs_by_dim[dx]) {
                            is HardStop -> return IntAffExpr(0)
                            is Wrap -> reduced_ndix.add(dx % gp.grid_size[dx])
                        }
                    }
                }
                return base_map[reduced_ndix]!![tile]!!
            }
        }
    }

    context(NodeCtx)
    private fun lp_set_up_potentials() {
        TODO()
    }

    context(NodeCtx)
    private fun lp_set_up_flows() {
        TODO()
    }

    /**
     * This is a heavy lifter of the interface between Grid World and LP world.
     *
     * This function encodes the entirety of the information needed to convert between an abstract
     * boolean algebra expression to a witnessing Relaxed Integer Logic LP expression, with correct
     * node handling.
     *
     * Leaves are handled by dispatch to the predicate compiler.
     */
    context(NodeCtx)
    private fun ril_compile_algebra(ch: GridLPChart, predicate_algebra: BAGP): LPAffExpr<Int> {
        return when (val spa = predicate_algebra.simplify()) {
            is True -> IntAffExpr(1)
            is False -> IntAffExpr(0)
            is Pred -> spa.x.lp_compile(ch)
            // this is pass-through, do not attach
            is Not -> RIL.not(ril_compile_algebra(ch, spa.x))
            // cost is the same, so we can minimize the implementation
            is And -> ril_compile_algebra(ch, spa.de_morganize())
            // it's somewhat arbitrary if we should add nodes here, but we add them for
            // debugability
            is Or -> RIL.or(spa.xs.map { x -> branch { ril_compile_algebra(ch, x) } })
            is Xor -> RIL.xor(spa.xs.map { x -> branch { ril_compile_algebra(ch, x) } })
            is Implies -> {
                val ril_x = branch { ril_compile_algebra(ch, spa.x) }
                val ril_y = branch { ril_compile_algebra(ch, spa.y) }
                RIL.implies(ril_x, ril_y)
            }
            is Eq -> {
                val ril_x = branch { ril_compile_algebra(ch, spa.x) }
                val ril_y = branch { ril_compile_algebra(ch, spa.y) }
                RIL.equiv(ril_x, ril_y)
            }
        }
    }
}
