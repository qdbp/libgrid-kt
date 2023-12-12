package grid_model.extents

import grid_model.Entity
import grid_model.FermionicTile
import grid_model.Plane
import grid_model.Shape
import grid_model.dimension.Dim
import grid_model.predicate.SinglePointCondition

/** A basic extent that spreads a fermionic (no overlaps allowed) tile demand over a given shape. */
data class ShapedFermionicExtent<D : Dim<D>, P : Plane>(
    private val entity: Entity,
    val shape: Shape<D>,
    val predicate: SinglePointCondition
) : SimpleShapedExtent<P>() {
    override fun get_active_tiles(): List<FermionicTile> =
        shape.points.map { FermionicTile(entity, it) }

    override fun get_point_demands(): Map<List<Int>, SinglePointCondition> =
        shape.points.associateWith { predicate }
}
