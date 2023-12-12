package grid_model

import boolean_algebra.And
import boolean_algebra.Implies
import boolean_algebra.Pred
import grid_model.dimension.Dim
import grid_model.dimension.Vec
import grid_model.extents.NullExtent
import grid_model.predicate.BaseGridPredicate
import grid_model.predicate.IsEntity
import mdspan.ndindex

/**
 * This base class models a "grid problem", which is a constraint and optimization problem on a
 * "grid world".
 *
 * We aim to be generally useful, but as all good tools, this one is honed and refined by the set of
 * things it does not aim to do. With this in mind, we define a grid problem as at minimum
 * conforming to the Three Dogmas which limit the scope of our interest:
 *
 * TODO introduce concept of entity, plane, tile, extent
 *
 * Dogma 1: Discreteness:
 * - Each tile, for each plane, is either occupied or not. Each entity either exists at a tile or
 *   not.
 *
 * Dogma 2: Spatial Relativity
 * - no entity has any knowledge of the global shape of the grid. Its constraints are defined with
 *   respect to its local origin and have a statically known, finite reach. When embedded in a
 *   larger grid, these constraints co-vary with the entity's position.
 *
 *   (As an aside, it is this dogma more than any of the others that shapes what we consider a "grid
 *   problem" in this context. It precludes, e.g., constraints of the form "connected to the closest
 *   entity of the same type", since that requires an unbounded reach across physical space. We must
 *   express only terms such as "has an entity of the same type within 3 tiles".)
 *
 * Dogma 3: Logical (i.e. plane) Locality
 * - each entity has a finite set of [Plane]s that it knows about and places constraints on. It can
 *   be embedded in a larger problem that includes more planes, and its behavior will not observe or
 *   depend on these.
 *
 * There may be more restrictions that are imposed in practise owing to incompleteness of
 * implementation or technical limitations; however, the dogmas are fundamental and this toolkit
 * will never be extended to cover problems that violate them.
 */
abstract class GridProblem<D : Dim<D>>(val dim: D) {

    abstract val name: String

    abstract val grid_size: Vec<D>

    protected abstract fun get_entity_set(): Set<Entity>

    val entities: Set<Entity> by lazy { get_entity_set() }

    private val entity_extents: Map<Entity, Map<Plane, Extent<*>>> = run {
        entities.associateWith { entity ->
            entity
                .active_planes()
                .associateWith { plane -> entity.get_extent_within(plane) }
                .filterValues { it != NullExtent }
        }
    }

    private fun gather_tile_sets(): Map<Plane, List<Tile>> {
        val tile_sets = mutableMapOf<Plane, MutableSet<Tile>>()
        for (entity in get_entity_set()) {
            for (plane in entity.active_planes()) {
                for (tile in entity.get_extent_within(plane).active_tiles) {
                    tile_sets.getOrPut(plane) { mutableSetOf() }.add(tile)
                }
            }
        }
        return tile_sets.mapValues { (_, set) -> set.sortedBy { it.tile_name() } }
    }

    val ptc: PlaneTileChart =
        object : PlaneTileChart {
            // TODO obsolete this in favor of ptc
            private val tile_sets: Map<Plane, List<Tile>> by lazy { gather_tile_sets() }
            private val tile_plane_map =
                tile_sets.flatMap { (plane, tiles) -> tiles.map { it to plane } }.toMap()

            override fun all_planes(): Collection<Plane> = tile_sets.keys

            override fun tiles_of(plane: Plane): List<Tile> = tile_sets[plane]!!

            override fun plane_of(tile: Tile): Plane = tile_plane_map[tile]!!
        }

    /**
     * This function must provide a mapping from out-of-bounds values to boundary conditions.
     *
     * By default, hard-bounds all planes.
     */
    open fun boundary_conditions(p: Plane): List<BoundaryCondition> = List(dim.ndim) { HardStop }

    /**
     * Gets any "set-up" conditions for the grid, as predicates. e.g. tiles that are fixed ahead of
     * time, (in future) required flows or potentials.
     */
    abstract fun get_setup_predicates(): Iterable<BaseGridPredicate>

    val setup_predicates: BAGP by lazy { And(get_setup_predicates().map { Pred(it) }).simplify() }

    /**
     * This is "Phase 1" of the compilation of a Grid Problem.
     *
     * This function combines the initial conditions, boundary conditions and entity demands, tiled
     * over the extent of the grid to come up with a Boolean Algebra for which every satisfying
     * assignment corresponds to a valid grid placement according to the rules defined by the
     * extents and the boundary and initial conditions.
     *
     * IMPORTANT NOTE: this algebra will contain references to out-of-bound coordinates. It is up to
     * the LPChart to map these to the appropriate LP variables in the downstream compilation step.
     */
    fun generate_satisfaction_algebra(): BAGP {
        val point_predicates = mutableListOf<BAGP>()
        entity_extents.map { (ent, ext_map) ->
            for (entity_coords in ndindex(this.grid_size)) {
                val entity_exists = Pred(IsEntity(ent) at entity_coords)
                point_predicates.add(
                    // this really is the core logic of the whole grid: for every (entity, tile)
                    // entity at tile => surrounding tiles match entity's requirements
                    Implies(
                        entity_exists,
                        And(
                            ext_map.values.map { ext ->
                                ext.demands.fmap { it translated entity_coords }
                            }
                        )
                    )
                )
            }
        }
        return And(point_predicates).simplify()
    }
}

/**
 * Welcome to the lowliest of the low. Have to start somewhere, right?
 *
 * Our problem is as follows:
 *
 * we have a 7x7 box, with the corners missing and the center missing.
 *
 * We have as many as we want of each of three shapes: the square tetromino, worth 1 point the L
 * tetromino, worth 3 points and the pentomino cross, worth 0 points. But we have to have at least
 * one.
 *
 * Our objective is to use this fine library to find the highest scoring solution to this problem.
 */
// object JankyStuffer: GridProblem {
//     override val dimension: GridDimension = D2
//     override val name: String = "janky_stuffer"
//
//
// }
