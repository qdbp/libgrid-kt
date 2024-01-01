package grid_model.predicate

import grid_model.Tile
import grid_model.planes.Plane

/** This grid point has the given tile. */
data class HasTile(val plane: Plane, val tile: Tile) : SinglePointCondition
