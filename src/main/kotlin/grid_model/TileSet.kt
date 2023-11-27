package grid_model

/**
 * A TileSet models the set of all available tiles of a given category.
 *
 * This is distinct from a `Set<Tile>` which may be used to represent some subset of the available
 * tiles that has e.g., been pruned by some constraint.
 */
data class TileSet<T: Tile>(val available_tiles: Set<T>) : Set<T> by available_tiles {
    constructor(vararg tiles: T) : this(tiles.toSet())

    companion object {
        inline fun <reified T> from_enum(): TileSet<T> where T : Tile, T : Enum<T> {
            return TileSet(enumValues<T>().toSet())
        }
    }
}
