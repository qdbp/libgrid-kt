package grid_model.predicate

import grid_model.geom.Dim
import grid_model.geom.Vec

/**
 * Base interface for objects representing some single-point on the underlying objects.
 *
 * Precisely because the point is unspecified, this is not a grid predicate proper, but can be
 * turned into one when provided with a concrete coordinate.
 */
sealed interface PointPredicate {
    infix fun <D : Dim<D>> at(coords: Vec<D>): LPP<D> = LPP(coords, this)
}
