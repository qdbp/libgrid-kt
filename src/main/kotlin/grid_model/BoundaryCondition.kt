package grid_model

/**
 * A simple sealed hierarchy of boundary conditions.
 *
 * These are specified on a per-(plane, dimension) basis by grid problems, and are then used to
 * construct appropriate tile charts for use in the LP compiler.
 */
sealed class BoundaryCondition

/** No tiles of this plane are valid outside the grid bounds. */
data object HardStop : BoundaryCondition()

/** Stepping out of bounds along this dimension will wrap around back into range. */
data object Wrap : BoundaryCondition()
