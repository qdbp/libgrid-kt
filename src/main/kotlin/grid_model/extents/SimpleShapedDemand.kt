package grid_model.extents

import boolean_algebra.BEReducer
import boolean_algebra.BooleanExpr.Companion.or
import boolean_algebra.BooleanExpr.Companion.pred
import grid_model.*
import grid_model.geom.Dim
import grid_model.geom.Shape
import grid_model.geom.Vec
import grid_model.predicate.GridPredicate
import grid_model.predicate.HasAnyEntity
import grid_model.predicate.HasAnyTile
import grid_model.predicate.HasTileOrMasked
import grid_model.tiles.BosonTile
import grid_model.tiles.FermiTile
import grid_model.tiles.Tile

/**
 * A simple shaped extent takes the form of:
 *
 * f_bool(shape.points.map (f_pred))
 *
 * That is a simple boolean aggregation of identical point predicates applied over some shape.
 *
 * Though "simple" this framework is good enough for the vast majority of cases. Before writing a
 * more complicated extent, it should be preferred to use additional [grid_model.plane.Plane]s
 *
 * These should generally be created as anonymous classes from the extent builder.
 */
abstract class SimpleShapedDemand<D : Dim<D>> : Demand<D>() {

    abstract val relator: GPReducer<D>

    abstract val shape: Shape<D>

    abstract fun predicate(vec: Vec<D>): BEPP

    override fun expr(): BEGP<D> {
        return relator(shape.points.map { vec -> predicate(vec).fmap { pp -> pp at vec } })
    }
}

/**
 * An SSE whose predicate relates to asserting something about tiles in a particular plane.
 *
 * At this point we leave the nature of the tile unspecified.
 */
abstract class TileDemand<D : Dim<D>> : SimpleShapedDemand<D>() {
    abstract val tile_name: String

    abstract val allow_masked: Boolean

    abstract fun get_tile(point: Vec<D>): Tile

    override fun get_active_tiles(): Set<Tile> = shape.points.map { get_tile(it) }.toSet()

    final override fun predicate(vec: Vec<D>): BEPP =
        when (allow_masked) {
            true -> pred(HasTileOrMasked(get_tile(vec)))
            false -> pred(HasAnyTile(get_tile(vec)))
        }
}

abstract class FermiTileDemand<D : Dim<D>> : TileDemand<D>() {
    final override fun get_tile(point: Vec<D>) = FermiTile(tile_name, point)
}

abstract class BosonTileDemand<D : Dim<D>> : TileDemand<D>() {
    final override fun get_tile(point: Vec<D>) = BosonTile(tile_name)
}

/**
 * A Simple Shaped Demand whose predicate asserts that at least of of the given neighbor entities
 * exists in the given tile.
 */
abstract class HasAnyOfNeighbors<D : Dim<D>> : SimpleShapedDemand<D>() {
    abstract val neighbors: Set<Entity<D>>

    final override val relator: GPReducer<D> = ::or

    final override fun get_active_tiles(): Set<Tile> = setOf()

    final override fun predicate(vec: Vec<D>): BEPP = pred(HasAnyEntity(neighbors))
}
