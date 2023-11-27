package grid_model

/**
 * The Entity models a fundamental physical "thing" that may be placed into the world.
 *
 * This physicality is encoded in the fact that entities are tiles. The ontological plane of a
 * grid problem is the plane of entities and its tile type is Entity.
 */
abstract class Entity {
    /**
     * Returns the extent of the entity within the given plane.
     *
     * Note that the extent depends only on the type of the plane, and will be the same for all
     * planes of the same type. This is a fundamental property of planes and extents.
     */
    fun <T : Tile> get_extent_within(plane: Plane<T>): Extent? = null
}
