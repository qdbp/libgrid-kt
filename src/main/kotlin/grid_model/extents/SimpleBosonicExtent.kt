package grid_model.extents

import grid_model.*
import grid_model.dimension.Dim
import grid_model.dimension.Vec
import grid_model.predicate.HasTile
import grid_model.predicate.SinglePointCondition

/** A basic extent that spreads a bosonic (overlaps allowed) tile demand over a given shape. */
data class SimpleBosonicExtent<D : Dim<D>>(val entity_name: String, val shape: Shape<D>) :
    SimpleShapedExtent<D>() {

    private val avatar = BosonicTile(entity_name)

    override fun get_active_tiles(): List<Tile> = listOf(avatar)

    override fun get_point_demands(): Map<Vec<D>, SinglePointCondition> =
        shape.points.associateWith { HasTile(avatar) }
}
