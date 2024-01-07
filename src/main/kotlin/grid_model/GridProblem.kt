package grid_model

import boolean_algebra.BooleanExpr.Companion.and
import boolean_algebra.BooleanExpr.Companion.not
import boolean_algebra.BooleanExpr.Companion.pred
import boolean_algebra.BooleanExpr.Companion.sat_count
import boolean_algebra.True
import grid_model.geom.*
import grid_model.plane.Plane
import grid_model.predicate.HasAnyEntity
import grid_model.predicate.LPP
import grid_model.predicate.PointPredicate
import grid_model.tiles.Tile

class GridProblemMisspecifiedException(msg: String) : Exception(msg)

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

    // ABSTRACTS
    abstract val arena: Vec<D>

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
    protected open fun generate_boundary_conditions(): Map<Plane, List<BoundaryCondition>> = mapOf()

    /**
     * Gets any static requirements for the grid, as predicates.
     *
     * e.g. some minimum number of entities must exist; some tiles must be set to a particular value
     */
    protected open fun generate_requirement_predicates(): BEGP<D> = True

    /**
     * Generate the entity mask.
     *
     * This should be a shape that fits in the bounding box of the grid. Points that are part of the
     * shape will block entities.
     */
    protected open fun get_entity_mask(): Map<Entity<D>, Shape<D>> = mapOf()

    protected open fun get_default_entity_mask(): Shape<D> = empty(dim)

    /**
     * Generate the tile mask for the given plane.
     *
     * This should be a shape that fits in the bounding box of grid. No tiles from the given plane
     * will be placed at points belonging to the shape's point set.
     */
    protected open fun generate_tile_mask(): Map<Plane, Shape<D>> = mapOf()

    protected open fun get_default_plane_mask(): Shape<D> = empty(dim)

    /**
     * The objective of the problem will be as follows: maximize the score, where the score is
     * defined as
     *
     * Σ{P ∈ valuation_predicates} (1 if P is true 0 otherwise) * predicate_value
     */
    protected open fun get_valuation_predicates(): Map<BEGP<D>, Double> = mapOf()

    // BASIC STRUCTURE AND SETUP
    val bounds: BBox<D> by lazy { (arena - dim.ones()).to_origin_bb() }

    // ENTITIES
    // TODO setting this up up front is error prone and annoying; we should be able to infer the
    //  entity set while building the problem if we proceed carefully
    val entities: Set<Entity<D>> by lazy { get_entity_set() }

    private val entity_extents: Map<Entity<D>, Map<Plane, Demand<D>>> by lazy {
        entities.associateWith { e ->
            mutableMapOf<Plane, Demand<D>>().also { m ->
                e.active_planes().forEach { p -> e.tile_demands_for(p)?.let { m[p] = it } }
            }
        }
    }

    private fun gather_tile_sets(): Pair<Map<Plane, List<Tile>>, Map<Tile, Plane>> {
        val plane_to_tiles = mutableMapOf<Plane, MutableSet<Tile>>()
        val tile_to_plane = mutableMapOf<Tile, Plane>()
        for (entity in get_entity_set()) {
            for (plane in entity.active_planes()) {
                for (tile in entity.tile_demands_for(plane)?.active_tiles ?: setOf()) {
                    plane_to_tiles.getOrPut(plane) { mutableSetOf() }.add(tile)
                    if (tile_to_plane.containsKey(tile)) {
                        throw GridProblemMisspecifiedException(
                            "Tile $tile is already assigned to plane ${tile_to_plane[tile]}. " +
                                "In a given problem, each tile must belong to at most one plane."
                        )
                    }
                    tile_to_plane[tile] = plane
                }
            }
        }
        return plane_to_tiles.mapValues { (_, set) -> set.sortedBy { it.tile_name() } } to
            tile_to_plane
    }

    val index: GridIndex by lazy {
        object : GridIndex {
            private val tile_sets by lazy { gather_tile_sets() }

            override fun all_entities(): List<Entity<*>> = entities.sortedBy { it.name }

            override fun all_planes(): List<Plane> = tile_sets.first.keys.sortedBy { it.nice_name }

            override fun tiles_of(plane: Plane): List<Tile> = tile_sets.first[plane] ?: listOf()

            override fun plane_of(tile: Tile): Plane =
                tile_sets.second[tile]
                    ?: throw GridProblemMisspecifiedException(
                        "Tile $tile is not assigned to any plane."
                    )
        }
    }

    val requirement_algebra: BEGP<D> by lazy { generate_requirement_predicates() }

    /**
     * This is "Phase 1" of the compilation of a Grid Problem.
     *
     * This function combines entity demands across all entities and tiles them over the problem
     * arena to produce [boolean_algebra.BooleanExpr] for which every satisfying assignment
     * corresponds to a valid grid placement according to the rules defined by entity demands.
     *
     * IMPORTANT NOTE: this algebra will contain references to out-of-bound coordinates. It is up to
     * the LPChart to map these to the appropriate LP variables in the downstream compilation step
     * by incorporating the given boundary conditions.
     */
    private fun generate_satisfaction_algebra(): BEGP<D> =
        and(
            // at each point...
            this.bounds.flatMap { vec_ix ->
                // each possible entity at that point...
                entity_extents.flatMap entities_loop@{ (entity, demand_map) ->
                    // (unless it is masked, in which case "dead men make no demands")...
                    if (is_masked(vec_ix, entity)) return@entities_loop listOf(True)
                    // for each of the planes it participates in...
                    // TODO currently we don't use the Plane since we just read it off later through
                    //  the injective tile -> plane map we currently demand. However, we shouldn't
                    //  remove planes here yet, since with them in place it's not a long shot to remove
                    //  the injectivity requirement.
                    demand_map.values.map { demand ->
                        // satisfies the demands for that plane...
                        demand.ontology.relator(
                            pred(HasAnyEntity(entity) at vec_ix),
                            demand.expr().fmap { it translated vec_ix }
                        )
                    } + // and satisfies neighbor demands, if any
                        (entity.neighbors_demand()?.let { demand ->
                            demand.ontology.relator(
                                pred(HasAnyEntity(entity) at vec_ix),
                                demand.expr().fmap { it translated vec_ix }
                            )
                        } ?: True)
                }
            }
        )

    val sat_algebra: BEGP<D> by lazy { generate_satisfaction_algebra() }

    // == BOUNDARY CONDITIONS ==

    private val boundary_conditions: Map<Plane, List<BoundaryCondition>> by lazy {
        generate_boundary_conditions()
    }

    fun get_boundary_condition(plane: Plane): List<BoundaryCondition> =
        boundary_conditions.getOrDefault(plane, List(dim.ndim) { AsMasked })

    // == MASKING ==
    private val entity_mask: Map<Entity<D>, Shape<D>> by lazy { get_entity_mask() }

    private val plane_masks: Map<Plane, Shape<D>> by lazy { generate_tile_mask() }

    fun plane_mask(p: Plane): Shape<D> = plane_masks[p] ?: get_default_plane_mask()

    fun is_masked(ix: Vec<D>, plane: Plane): Boolean =
        plane_masks[plane]?.points?.contains(ix) ?: false

    fun is_masked(ix: Vec<D>, entity: Entity<*>): Boolean =
        (entity_mask[entity] ?: get_default_entity_mask()).points.contains(ix)

    // == OBJECTIVE ==
    val val_algebra: Map<BEGP<D>, Double> by lazy { get_valuation_predicates() }

    // convenience methods for building requirements, etc.

    fun for_all_points(op: (Vec<D>) -> PointPredicate): BEGP<D> =
        and(bounds.map { pred(LPP(it, op(it))) })

    private fun for_all_points_raw(op: (Vec<D>) -> BEGP<D>): BEGP<D> = and(bounds.map { op(it) })

    /** The given entity is present at exactly the given tiles and no others. */
    infix fun Entity<*>.exactly_at(shape: Shape<D>): BEGP<D> = for_all_points_raw { vec: Vec<D> ->
        when {
            shape.points.contains(vec) -> pred(HasAnyEntity(this) at vec)
            else -> not(pred(HasAnyEntity(this) at vec))
        }
    }

    fun val_entity_count(entity: Entity<*>, value: Double): Map<BEGP<D>, Double> =
        bounds.associateBy({ pred(HasAnyEntity(entity) at it) }, { value })

    fun req_entity_count(e: Entity<*>, min: Int = 0, max: Int = Int.MAX_VALUE): BEGP<D> =
        sat_count(bounds.map { pred(HasAnyEntity(e) at it) }, min, max)
}
