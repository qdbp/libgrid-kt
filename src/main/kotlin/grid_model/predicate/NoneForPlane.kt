package grid_model.predicate

import grid_model.plane.Plane

/**
 * No tile for the given plane is present at this point.
 *
 * This will be the go-to for setting boundary conditions.
 */
data class NoneForPlane(val plane: Plane) : PointPredicate
