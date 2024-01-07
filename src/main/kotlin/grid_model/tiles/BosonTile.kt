package grid_model.tiles

/**
 * Attn: this is *not* what you want for expressing "shaped things" -- see [FermiTile].
 *
 * A basic tile whose identity is determined by nothing but the name.
 */
data class BosonTile(val tile_name: String) : Tile {
    override fun tile_name(): String = tile_name

    override fun toString(): String = "basic:$tile_name"
}
