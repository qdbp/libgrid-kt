package grid_model

/**
 * A simple sealed hierarchy of boundary conditions.
 *
 * These are specified on a per-(plane, dimension) basis by grid problems, and are then used to
 * construct appropriate tile charts for use in the LP compiler.
 */
sealed class BoundaryCondition

/** Stepping out of bounds will be treated identical to a [grid_model.adapters.lp.Masked] value. */
data object AsMasked : BoundaryCondition()

/**
 * No tiles of this plane are valid outside the grid bounds, even for conditions that allow Masked
 * values. A hard False will be returned.
 */
data object HardStop : BoundaryCondition()

/** Stepping out of bounds along this dimension will wrap around back into range. */
data object Wrap : BoundaryCondition()

/** Stepping out of bounds will be assigned a new unconstrained variable. */
data object Free : BoundaryCondition()
