package grid_model.adapters.lp

import grid_model.Tile
import grid_model.planes.Plane
import kulp.expressions.LPBinaryExpr

fun interface LPTileChart {
    /** The bounded expression should be binary-like: 0 if tile is absent, 1 if present. */
    operator fun get(grid_ndix: List<Int>, plane: Plane, tile: Tile): LPBinaryExpr
}
