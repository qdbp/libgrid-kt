package grid_model.adapters.lp

import boolean_algebra.*
import grid_model.*
import grid_model.adapters.lp.ril_compile.GridPredicateRILCompiler.Companion.ril_compile
import grid_model.dimension.Dim
import kulp.*
import kulp.aggregates.LPOneOfN
import kulp.expressions.IntAffExpr
import kulp.expressions.LPBinaryExpr
import kulp.expressions.Zero
import kulp.ril.RIL
import kulp.variables.LPBinary

// TODO right now we hardcode LocatedPointPredicate, but in reality we need to be able to handle
//  a collection of predicates. The GridProblem will return
// TODO currently we're too tightly coupled to the GridProblem
//  e.g. we can't currently have a pure grid-level simplification/optimization pass between the
//  grid and adapter. We need to introduce an intermediate interface between the grid problem and
//  adapter that has: - entities - predicates - boundary conditions - objective - masks
class GridLPAdapter<D : Dim<D>>(val gp: GridProblem<D>) {

    val lp_prob: LPProblem by lazy {
        object : GridLPProblem() {

            override val parent: GridProblem<*> = gp

            val e_chart = node { lp_set_up_entities() }
            val t_chart = node { lp_set_up_tiles() }
            // TODO lp_set_up_potentials()
            // TODO lp_set_up_flows()

            override val chart = GridLPChart(gp.index, e_chart, t_chart)

            init {
                node.branch("sat_al") {
                    chart.run { "is_sat" { ril_compile_algebra(gp.sat_algebra) ge 1 } }
                }
                node.branch("req_al") {
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
    }

    context(NodeCtx)
    private fun lp_set_up_entities(): LPEntityChart {
        // entities are not mutually exclusive, so we add a free LPBinary for each (entity, ix)
        val chart_map = mutableMapOf<List<Int>, MutableMap<Entity<*>, LPBinaryExpr>>()
        for (entity in gp.entities) {
            for (ndix in gp.bounds) {
                val evar =
                    when {
                        gp.is_masked(ndix, entity) -> Zero
                        else -> "${entity.name}_${ndix.lp_name}" { LPBinary().lift01() }
                    }
                chart_map.getOrPut(ndix) { mutableMapOf() }[entity] = evar
            }
        }
        return LPEntityChart { ndix, entity -> chart_map[ndix]!![entity]!! }
    }

    /** Sets up the fundamental backing LP variables of the problem */
    context(NodeCtx)
    private fun lp_set_up_tiles(): LPTileChart {

        // this is the "base map" of in-bounds tiles, which we will then extend with boundary
        // conditions
        val base_map: MutableMap<Pair<List<Int>, Tile>, LPBinaryExpr> = mutableMapOf()

        for (plane in gp.index.all_planes()) {
            val tiles = gp.index.tiles_of(plane)
            // + 1, since we need to add a "no tile" option. By convention, this will be the
            // last lpvar in the one-of-n aggregate. No one outside this class should care.
            val one_of_n = "tile_${plane}" {
                // constrained along last dim by default
                LPOneOfN(
                    gp.shape + (tiles.size + 1),
                    mask =
                        gp.plane_mask(plane).points.flatMap { tiles.indices.map { tx -> it + tx } }
                )
            }
            // note: no + 1 here, since we do not add the "no tile" binary option to the chart
            for (ndix in gp.bounds) {
                for (tx in tiles.indices) {
                    val tile = tiles[tx]
                    base_map[ndix to tile] = one_of_n[ndix + tx]
                }
            }
        }

        return LPTileChart { ndix, p, t ->
            val bcs = gp.boundary_conditions(p)
            val reduced_ndix = mutableListOf<Int>()
            for (dx in ndix.indices) {
                val dim_ix = ndix[dx]
                when {
                    // in range -> add to reduced_ndix
                    dim_ix in 0 ..< gp.shape[dx] -> reduced_ndix.add(dim_ix)
                    // out of range -> apply boundary conditions
                    bcs[dx] is HardStop -> return@LPTileChart Zero
                    bcs[dx] is Wrap -> reduced_ndix.add(dx % gp.shape[dim_ix])
                }
            }
            base_map[reduced_ndix to t]!!
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
    private fun ril_compile_algebra(predicate_algebra: BEGP): LPAffExpr<Int> {
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
                val min_branch =
                    when {
                        // special case only those cases that let us skip ril-compiling the terms
                        // at all. Any extra cases are optimized by RIL itself.
                        pa.min_sat <= 0 -> RIL.always
                        pa.min_sat > pa.xs.size -> RIL.never
                        else -> {
                            branch("min_sat") {
                                val ril_terms = pa.xs.branch_each { ril_compile_algebra(it) }
                                RIL.min_sat(pa.min_sat, ril_terms)
                            }
                        }
                    }
                val max_branch =
                    when {
                        pa.max_sat >= pa.xs.size -> RIL.always
                        pa.max_sat < 0 -> RIL.never
                        else -> {
                            branch("max_sat") {
                                val compiled_terms = pa.xs.branch_each { ril_compile_algebra(it) }
                                RIL.max_sat(pa.max_sat, compiled_terms)
                            }
                        }
                    }
                RIL.and(min_branch, max_branch)
            }
        }
    }
}
