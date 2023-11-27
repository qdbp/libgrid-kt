package grid_model.planes

import grid_model.Entity
import grid_model.Tile
import grid_model.TileSet
import grid_model.UniversalPlane

/**
 * The special plane representing the existence or non-existence of a given entity.
 *
 * This plane is common to all grid problems, and is used by the base class directly.
 *
 * By convention, extents should treat the object as being in the (0, 0, ...) corner of the extent.
 * This is not always possible for more complicated shapes.
 */
class OntologicalPlane<T: Tile>(entity_tile_set: TileSet<T>) : UniversalPlane<T>(entity_tile_set)
