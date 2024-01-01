package grid_model

import grid_model.planes.Plane

/**
 * The index collates a complete inventory of all planes, tiles, entities, or other objects.
 *
 * Importantly, the outputs are lists whose order matters. An object's position in the list will be
 * used to consistently map it into e.g. a list of binary variables.
 */
interface GridIndex {
    fun all_entities(): List<Entity<*>>

    fun all_planes(): List<Plane>

    // need list here because the order matters and must be fixed
    fun tiles_of(plane: Plane): List<Tile>

    val all_tiles: Set<Tile>
        get() = all_planes().flatMap { tiles_of(it) }.toSet()
}
