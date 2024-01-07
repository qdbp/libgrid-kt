package grid_model

import grid_model.extents.HasAnyOfNeighbors
import grid_model.extents.TileDemand
import grid_model.geom.Dim
import grid_model.plane.Plane

/**
 * The Entity models a fundamental "thing" that may be placed into the world.
 *
 * This physicality is encoded in the fact that entities are tiles. The ontological plane of a grid
 * problem is the plane of entities and its tile type is Entity.
 */
interface Entity<D : Dim<D>> {
    val name: String
        get() = javaClass.simpleName

    /** Each entity must provide a list of planes it is active in. */
    fun active_planes(): Collection<Plane>

    /**
     * Returns the tile extent of the entity within the given plane.
     *
     * Note that the extent depends only on the type of the plane, and will be the same for all
     * planes of the same type. This is a fundamental property of planes and extents.
     *
     * This function must be a pure and idempotent operation. It will be called an arbitrary number
     * of times during the lifetime of the entity.
     */
    fun <P : Plane> tile_demands_for(plane: P): TileDemand<D>? = null

    /** Returns the entity extent that must be satisfied against neighboring entities. */
    fun neighbors_demand(): HasAnyOfNeighbors<D>? = null
}
