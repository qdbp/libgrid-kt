package grid_model.tiles

import kulp.lp_name

/**
 * Tiles of this instance have, for a given name, a separate identity for each passed point.
 *
 * This will usually be each relative coordinate in the shape of an extent.
 *
 * This expresses that e.g. the top left corner of some square is logically distinguishable from the
 * lower right corner (because if they were not, you could have a 2x2 square at every point by
 * filling the space with the "undifferentiated tile"!), even if they are not distinguishable by
 * some sort of "physical substance".
 */
// NB it is very important to keep this a data class.
data class FermiTile(val name: String, val vec: List<Int>) : Tile {
    override fun tile_name(): String = "${name}_${vec.lp_name}"

    override fun toString(): String = "fermi:$name$vec"
}
