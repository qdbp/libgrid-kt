package grid_model

import boolean_algebra.BooleanExpr
import grid_model.dimension.Dim
import grid_model.planes.Plane
import grid_model.predicate.SPGP

// TODO lift to interface?
/**
 * An extent is a set of local constraints imposed on tiles by the existence of an entity.
 *
 * As per dogma 2, the Extent has no knowledge of its absolute coordinates in the grid, or the size
 * of the grid. All of its constraints are always relative to the origin of the extent.
 *
 * By itself, an [Extent] is a [Plane]-agnostic. One can think of it as a generalization of a pure
 * [grid_model.Shape] which instead of simple points, has some logical conditions on what tiles are
 * valid and invalid in what arrangements.
 *
 * Thus, [Extent]s may be rendered in any given [Plane]. Implementations will pass this plane to
 * individual predicate constructors, which usually *do* require a concrete plane to be fully
 * specified.
 *
 * Implementers are encouraged to make flexible intermediate Extent classes that can be usefully
 * specialized to many different planes.
 */
abstract class Extent<D : Dim<D>> {

    /** Enumerates all the tiles this extent will use. */
    protected abstract fun get_active_tiles(): List<Tile>

    /** Given a plane, renders the concrete demands of this extent within that plane. */
    abstract fun render_demands_within(plane: Plane): BooleanExpr<SPGP<D>>

    val active_tiles: List<Tile> by lazy { get_active_tiles() }
}
