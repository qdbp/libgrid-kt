package grid_model

/**
 * Interface for providers of mappings between (grid_coords, tile) and an LPBinary.
 *
 * We use the name "chart" to avoid terms like 'assignment' or 'mapping' which both carry confusing
 * connotations.
 */
context(GridDimension)
interface GridChart<T> {

    /**
     * Note, we leave this abstract because we might want to e.g. have wrapping, clipping, or other
     * non-trivial behavior.
     */
    operator fun get(coords: List<Int>): PointTileMapping<T>
}

interface PointTileMapping<T> : Map<Tile, T> {}
