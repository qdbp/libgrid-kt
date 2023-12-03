package grid_model

/**
 * Connected-Component Potential.
 *
 * A MIP formulation of the notion of having entities required to form a connected graph.
 */
interface Potential<T: Tile> {

    /**
     * A potential of this type at a grid cell if and only if the cell contains one
     * of these tiles.
     */
    fun active_tiles(): Set<T>
}
