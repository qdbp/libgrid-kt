package grid_model

/**
 * A TileSet models the set of all available tiles of a given category.
 *
 * This is distinct from a `Set<Tile>` which may be used to represent some subset of the available
 * tiles that has e.g., been pruned by some constraint.
 */
data class TileSet<T : Tile>(val available_tiles: Set<T>) : Set<T> by available_tiles {
    constructor(vararg tiles: T) : this(tiles.toSet())

    companion object {
        inline fun <reified T> from_enum(): TileSet<T> where T : Tile, T : Enum<T> {
            return TileSet(enumValues<T>().toSet())
        }
    }
}

/**
 * A TileUniverse models the set of all available tiles of all categories.
 *
 * This object must be assembled by the solver based on the tiles declared by the set of entities
 * that we are trying to place, and is then passed to methods that compile abstract tile constraints
 * into concrete LP expressions.
 */
data class TileUniverse(val tile_sets: List<TileSet<*>>) {

    val allTiles: Set<Tile>

    init {
        val allTiles = mutableSetOf<Tile>()
        for (tile in tile_sets.flatMap { it }) {
            if (tile in allTiles) {
                throw IllegalArgumentException("Duplicate tile: $tile")
            }
        }
        this.allTiles = allTiles
    }

    fun size(): Int {
        return allTiles.size
    }
}
