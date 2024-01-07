package grid_model.adapters.lp

import grid_model.tiles.Tile
import grid_model.plane.Plane

fun interface LPTileChart {
    /** The bounded expression should be binary-like: 0 if tile is absent, 1 if present. */
    operator fun get(vec_ix: List<Int>, plane: Plane, tile: Tile): LPChartEntry
}
