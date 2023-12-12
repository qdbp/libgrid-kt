package grid_model

import grid_model.extents.NullExtent

/**
 * The Entity models a fundamental physical "thing" that may be placed into the world.
 *
 * This physicality is encoded in the fact that entities are tiles. The ontological plane of a grid
 * problem is the plane of entities and its tile type is Entity.
 */
interface Entity {
    val name: String

    /** Each entity must provide a list of planes it is active in. */
    fun active_planes(): List<Plane>

    /**
     * Returns the extent of the entity within the given plane.
     *
     * Note that the extent depends only on the type of the plane, and will be the same for all
     * planes of the same type. This is a fundamental property of planes and extents.
     *
     * This function must be a stateless operation. It will be called an arbitrary number of times
     * during the lifetime of the entity.
     */
    // todo make this an abstract val Map?
    fun <P : Plane> get_extent_within(plane: P): Extent<P> = NullExtent
}
