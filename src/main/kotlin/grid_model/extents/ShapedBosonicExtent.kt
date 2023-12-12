package grid_model.extents

import grid_model.*
import grid_model.dimension.Dim
import grid_model.predicate.SinglePointCondition

/** A basic extent that spreads a bosonic (overlaps allowed) tile demand over a given shape. */
data class ShapedBosonicExtent<D : Dim<D>, P : Plane>(
    private val entity: Entity,
    val shape: Shape<D>,
    val predicate: SinglePointCondition
) : SimpleShapedExtent<P>() {

    private val avatar = BosonicTile(entity)

    override fun get_active_tiles(): List<Tile> = listOf(avatar)

    override fun get_point_demands(): Map<List<Int>, SinglePointCondition> =
        shape.points.associateWith { predicate }
}
