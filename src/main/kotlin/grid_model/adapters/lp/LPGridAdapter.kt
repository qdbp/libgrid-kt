package grid_model.adapters.lp

import boolean_algebra.*
import grid_model.*
import grid_model.adapters.lp.pc.ril_compile
import grid_model.dimension.Dim
import kulp.*
import kulp.aggregates.LPOneOfN
import kulp.expressions.IntAffExpr
import kulp.expressions.IntConstExpr
import kulp.transforms.ril.RIL
import kulp.variables.LPBinary

// TODO right now we hardcode LocatedPointPredicate, but in reality we need to be able to handle
//  a collection of predicates. The GridProblem will return
class LPGridAdapter<D : Dim<D>>(val gp: GridProblem<D>) {

    var chart: GridLPChart

    val lp_prob: LPProblem =
        object : LPProblem() {
            val e_chart = node { lp_set_up_entities() }
            val t_chart = node { lp_set_up_tiles() }

            // TODO lp_set_up_potentials()
            // TODO lp_set_up_flows()

            val chart = GridLPChart(e_chart, gp.plane_tile_chart, t_chart)

            init {
                this@LPGridAdapter.chart = chart
            }

            init {
                val cxt_expr =
                    node.branch("cxt") {
                        chart.run { "is_sat" { ril_compile_algebra(gp.sat_algebra) ge 1 } }
                    }
                val init_expr =
                    node.branch("init") {
                        chart.run { "is_sat" { ril_compile_algebra(gp.requirement_algebra) ge 1 } }
                    }
            }

            val obj: LPAffExpr<Double> =
                node.branch("obj") {
                    chart.run {
                        gp.val_algebra
                            .map { branch { it.value * ril_compile_algebra(it.key).relax() } }
                            .lp_sum()
                    }
                }

            override fun get_objective(): LPObjective = obj to LPObjectiveSense.Maximize

            // todo need some way to extract the tiles lol
            // something with signature ix -> Set<Tile> and ix -> Entity?

        }

    context(NodeCtx)
    private fun lp_set_up_entities(): LPEntityChart {
        // entities are not mutually exclusive, so we add a free LPBinary for each (entity, ix)
        val chart_map = mutableMapOf<List<Int>, MutableMap<Entity<*>, LPBinary>>()
        for (entity in gp.entities) {
            for (ndix in gp.bounds) {
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
        val ptc = gp.plane_tile_chart

        // this is the "base map" of in-bounds tiles, which we will then extend with boundary
        // conditions
        val base_map: MutableMap<Pair<List<Int>, Tile>, LPBinary> = mutableMapOf()

        for (plane in ptc.all_planes()) {
            val tiles = ptc.tiles_of(plane)
            // + 1, since we need to add a "no tile" option. By convention, this will be the
            // last lpvar in the one-of-n aggregate. No one outside this class should care.
            val one_of_n = "tile_${plane}" {
                // constrained along last dim by default
                LPOneOfN(gp.shape + (tiles.size + 1))
            }
            // note: no + 1 here, since we do not add the "no tile" binary option to the chart
            for (ndix in gp.bounds) {
                for (tx in tiles.indices) {
                    val tile = tiles[tx]
                    base_map[ndix to tile] = one_of_n[ndix + tx]
                }
            }
        }

        // now we wrap the base map with an appropriate `withDefault` to handle boundary conditions
        val bconds_by_tile: Map<Tile, List<BoundaryCondition>> =
            ptc.all_planes()
                .flatMap { p -> ptc.tiles_of(p).map { p to it } }
                .associate { (p, t) -> t to gp.boundary_conditions(p) }

        return LPTileChart { ndix, tile ->
            val bcs = bconds_by_tile[tile]!!
            val reduced_ndix = mutableListOf<Int>()
            for (dx in ndix.indices) {
                val dim_ix = ndix[dx]
                when {
                    // in range -> add to reduced_ndix
                    dim_ix in 0 ..< gp.shape[dx] -> reduced_ndix.add(dim_ix)
                    // out of range -> apply boundary conditions
                    bcs[dx] is HardStop -> return@LPTileChart IntConstExpr(0)
                    bcs[dx] is Wrap -> reduced_ndix.add(dx % gp.shape[dim_ix])
                }
            }
            base_map[reduced_ndix to tile]!!
        }
        // todo might be worth memoizing this lambda, add a generic 'memo' extension on func types
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
    context(NodeCtx, GridLPChart)
    private fun ril_compile_algebra(predicate_algebra: BAGP): LPAffExpr<Int> {
        return when (val pa = predicate_algebra) {
            is True -> IntAffExpr(1)
            is False -> IntAffExpr(0)
            is Pred -> pa.atom.ril_compile()
            is Not -> RIL.not(ril_compile_algebra(pa.x))
            is And -> RIL.and(pa.xs.branch_each("and") { ril_compile_algebra(it) })
            is Or -> RIL.or(pa.xs.branch_each("or") { ril_compile_algebra(it) })
            is Xor -> RIL.xor(pa.xs.branch_each("xor") { ril_compile_algebra(it) })
            is Implies -> {
                val ril_p = branch("impl_p") { ril_compile_algebra(pa.p) }
                val ril_q = branch("impl_q") { ril_compile_algebra(pa.q) }
                RIL.implies(ril_p, ril_q)
            }
            is Eq -> {
                val ril_a = branch("eq_a") { ril_compile_algebra(pa.a) }
                val ril_b = branch("eq_b") { ril_compile_algebra(pa.b) }
                RIL.equiv(ril_a, ril_b)
            }
            is SatCount -> {
                val terms = pa.xs
                val min_branch =
                    when {
                        // special case only those cases that let us skip ril-compiling the terms
                        // at all. Any extra cases are optimized by RIL itself.
                        pa.min_sat <= 0 -> RIL.always
                        pa.min_sat > terms.size -> RIL.never
                        else -> {
                            branch("min_sat") {
                                val ril_terms = terms.branch_each { ril_compile_algebra(it) }
                                RIL.min_sat(pa.min_sat, ril_terms)
                            }
                        }
                    }
                val max_branch =
                    when {
                        pa.max_sat >= terms.size -> RIL.always
                        pa.max_sat < 0 -> RIL.never
                        else -> {
                            branch("max_sat") {
                                val compiled_terms = terms.branch_each { ril_compile_algebra(it) }
                                RIL.max_sat(pa.max_sat, compiled_terms)
                            }
                        }
                    }
                RIL.and(min_branch, max_branch)
            }
        }
    }
}
