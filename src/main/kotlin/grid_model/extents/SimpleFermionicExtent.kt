package grid_model.extents

import grid_model.FermionicTile
import grid_model.Shape
import grid_model.dimension.Dim
import grid_model.dimension.Vec
import grid_model.planes.Plane
import grid_model.predicate.HasTile
import grid_model.predicate.SinglePointCondition

/** A basic extent that spreads a fermionic (no overlaps allowed) tile demand over a given shape. */
data class SimpleFermionicExtent<D : Dim<D>>(val entity_name: String, val shape: Shape<D>) :
    SimpleShapedExtent<D>() {

    override fun get_active_tiles(): List<FermionicTile> =
        shape.points.map { FermionicTile(entity_name, it) }

    override fun get_point_demands(plane: Plane): Map<Vec<D>, SinglePointCondition> =
        shape.points.associateWith { HasTile(plane, FermionicTile(entity_name, it)) }
}
