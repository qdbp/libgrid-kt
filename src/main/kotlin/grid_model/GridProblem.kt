package grid_model

import boolean_algebra.BooleanExpr.Companion.and
import boolean_algebra.BooleanExpr.Companion.implies
import boolean_algebra.BooleanExpr.Companion.pred
import boolean_algebra.BooleanExpr.Companion.sat_count
import boolean_algebra.True
import grid_model.Shape.Companion.empty
import grid_model.dimension.BBox
import grid_model.dimension.Dim
import grid_model.dimension.Vec
import grid_model.planes.Plane
import grid_model.predicate.IsEntity

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

    open val name: String = javaClass.simpleName

    abstract val bounds: BBox<D>

    // ABSTRACTS

    /** This function must enumerate the set of considered entities. */
    protected abstract fun get_entity_set(): Set<Entity<D>>

    /**
     * This function must specify what happens if a tile would be placed outside the grid by an
     * entity at a candidate location.
     *
     * By default, hard-bounds all planes: no tile may be placed outside the grid.
     *
     * See [BoundaryCondition] for supported behaviors. Behaviors are applied per dimension.
     */
    open fun boundary_conditions(p: Plane): List<BoundaryCondition> = List(dim.ndim) { HardStop }

    /**
     * Gets any static requirements for the grid, as predicates.
     *
     * e.g. some minimum number of entities must exist; some tiles must be set to a particular value
     */
    protected open fun generate_requirement_predicates(): BEGP = True

    /**
     * Generate the entity mask.
     *
     * This should be a shape that fits in the bounding box of the grid. Points that are part of the
     * shape will block entities.
     */
    protected open fun generate_entity_mask(): Shape<D> = empty(dim)

    /**
     * Generate the tile mask for the given plane.
     *
     * This should be a shape that fits in the bounding box of grid. No tiles from the given plane
     * will be placed at points belonging to the shape's point set.
     */
    protected open fun generate_plane_mask(plane: Plane): Shape<D> = empty(dim)

    /**
     * The objective of the problem will be as follows: maximize the score, where the score is
     * defined as
     *
     * Σ{P ∈ valuation_predicates} (1 if P is true 0 otherwise) * predicate_value
     */
    protected open fun get_valuation_predicates(): Map<BEGP, Double> = mapOf()

    // BASIC STRUCTURE AND SETUP
    val shape
        get() = bounds.shape

    // ENTITIES
    val entities: Set<Entity<D>> by lazy { get_entity_set() }

    private val entity_extents: Map<Entity<D>, Map<Plane, Extent<D>>> by lazy {
        entities.associateWith { e ->
            mutableMapOf<Plane, Extent<D>>().also { m ->
                e.active_planes().forEach { p -> e.get_extent_within(p)?.let { m[p] = it } }
            }
        }
    }

    private fun gather_tile_sets(): Map<Plane, List<Tile>> {
        val tile_sets = mutableMapOf<Plane, MutableSet<Tile>>()
        for (entity in get_entity_set()) {
            for (plane in entity.active_planes()) {
                for (tile in entity.get_extent_within(plane)?.active_tiles ?: setOf()) {
                    tile_sets.getOrPut(plane) { mutableSetOf() }.add(tile)
                }
            }
        }
        return tile_sets.mapValues { (_, set) -> set.sortedBy { it.tile_name() } }
    }

    val index: GridIndex by lazy {
        object : GridIndex {
            private val tile_sets: Map<Plane, List<Tile>> by lazy { gather_tile_sets() }

            override fun all_entities(): List<Entity<*>> = entities.sortedBy { it.name }

            override fun all_planes(): List<Plane> = tile_sets.keys.sortedBy { it.nice_name }

            override fun tiles_of(plane: Plane): List<Tile> = tile_sets[plane]!!
        }
    }

    val requirement_algebra: BEGP by lazy { generate_requirement_predicates() }

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
    private fun generate_satisfaction_algebra(): BEGP {
        val point_predicates = mutableListOf<BEGP>()
        entity_extents.map { (ent, ext_map) ->
            for (ent_vec in this.bounds) {
                val entity_exists = pred(IsEntity(ent) at ent_vec)
                point_predicates.add(
                    // this really is the core logic of the whole grid: for every (entity, tile)
                    // entity at tile => surrounding tiles match entity's requirements
                    implies(
                        entity_exists,
                        and(
                            ext_map.entries.map { (pln, ext) ->
                                ext.render_demands_within(pln).fmap { it translated ent_vec }
                            }
                        )
                    )
                )
            }
        }
        return and(point_predicates)
    }

    val sat_algebra: BEGP by lazy { generate_satisfaction_algebra() }

    // == MASKING ==
    private val entity_mask: Shape<D> by lazy { generate_entity_mask() }

    private val plane_masks: Map<Plane, Shape<D>> by lazy {
        index.all_planes().associateWith { p -> generate_plane_mask(p) }
    }

    fun plane_mask(p: Plane): Shape<D> = plane_masks[p] ?: empty(dim)

    fun is_masked(ix: Vec<D>, plane: Plane): Boolean =
        plane_masks[plane]?.points?.contains(ix) ?: false

    fun is_masked(ix: Vec<D>, entity: Entity<*>): Boolean = entity_mask.points.contains(ix)

    // == OBJECTIVE ==
    val val_algebra: Map<BEGP, Double> by lazy { get_valuation_predicates() }

    fun val_entity_count(entity: Entity<*>, value: Double): Map<BEGP, Double> =
        bounds.associateBy({ pred(IsEntity(entity) at it) }, { value })

    fun req_entity_count(e: Entity<*>, min: Int = 0, max: Int = Int.MAX_VALUE): BEGP =
        sat_count(bounds.map { pred(IsEntity(e) at it) }, min, max)
}
