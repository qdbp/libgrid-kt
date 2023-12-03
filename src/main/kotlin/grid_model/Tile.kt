package grid_model

/**
 * A tile is the atomic constituent of a grid cell.
 *
 * Each tile is either present or not present -- ultimately, it maps to a binary variable.
 *
 * Tiles may have same-point or local-grid dependencies.
 * For same-point dependencies, see [PointTileConstraint]
 * For local-grid dependencies, see [Extent]
 */
interface Tile {
}
