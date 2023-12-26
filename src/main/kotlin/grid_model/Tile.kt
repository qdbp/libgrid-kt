package grid_model

import kulp.lp_name

/**
 * A tile is the atomic constituent of a grid cell.
 *
 * Each tile is either present or not present -- ultimately, it maps to a binary variable.
 *
 * Not all tiles are mutually exclusive. Sets of mutually exclusive tiles are encapsulated as
 * [Plane]s. There is a strict one-to-many mapping between [Plane]s and [Tile]s, and each tile
 * belongs to exactly one plane.
 */
interface Tile {

    /** A slightly more verbose name to avoid overloading on enum's `name`. */
    fun tile_name(): String
}

/**
 * Shortcut for entity-linked tiles that can overlap between entities.
 *
 * Example: power-plane coverage by electric poles, cachement areas, etc.
 */
data class BosonicTile(val name: String) : Tile {
    override fun tile_name(): String = name
}

/**
 * Shortcut for entity-linked tiles that exclude one another.
 *
 * e.g the body tiles of a solid geometric shape: a right corner cannot overlap a left corner, so
 * they must occupy identity "levels".
 *
 * TODO make these Vec? really not much value threading the dim genericity this low, we can
 *  probably assume we have it right at this point and just use List<Int>
 */
data class FermionicTile(val name: String, val vec: List<Int>) : Tile {
    override fun tile_name(): String = "${name}_${vec.lp_name}"
}
