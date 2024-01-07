package grid_model.predicate

import grid_model.tiles.Tile

// TODO relax to HasAnyTile and make HasTile a special case of it
/** This grid point has the given tile. */
data class HasAnyTile(val tiles: Collection<Tile>) : PointPredicate {
    constructor(vararg tiles: Tile) : this(tiles.toList())
}

/**
 * This grid point has the given tile, or is masked.
 *
 * Note that we do not support lists of tiles here for the reason that masking is per-plane, and
 * since each tile can belong to a different plane, we would potentially be silently querying
 * multiple planes' masks. This is unlikely to be intended and can lead to buggy behavior.
 */
data class HasTileOrMasked(val tile: Tile) : PointPredicate
