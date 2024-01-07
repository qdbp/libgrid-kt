package grid_model.tiles

import grid_model.plane.Plane

/**
 * A tile is the atomic constituent of a grid cell.
 *
 * Each tile is either present or not present -- ultimately, it maps to a binary variable.
 *
 * Not all tiles are mutually exclusive. Sets of mutually exclusive tiles are encapsulated as
 * [Plane]s. There is a strict one-to-many mapping between [Plane]s and [Tile]s, and each tile
 * belongs to exactly one plane.
 *
 * Note to implementers: all tile instances MUST behave like data classes. Their equality and hash
 * must be identical when they hold semantically identical data.
 */
// this interface is sealed because it's easy to screw up tile implementations by not making them
// data-like. One should use the provided implementations, and wrap if metadata is needed.
sealed interface Tile {
    fun tile_name(): String
}
