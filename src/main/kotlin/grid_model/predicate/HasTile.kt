package grid_model.predicate

import grid_model.Tile

/** This grid point has the given tile. */
data class HasTile(val tile: Tile) : SinglePointCondition
