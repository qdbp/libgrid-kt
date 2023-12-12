package grid_model

/**
 * Connected-Component Potential.
 *
 * A MIP formulation of the notion of having entities required to form a connected graph.
 */
interface Potential<T : Tile> {

    /**
     * A potential of this type at a grid cell if and only if the cell contains one of these tiles.
     */
    fun active_tiles(): Set<T>

    /**
     * Maximum depth of the spanning tree found by the potential for each CC.
     *
     * In effect, this is the maximum depth of all spanning trees, since this formulation is able to
     * find the shallowest spanning tree. However, it is likely that making this value smaller will
     * worsen performance.
     *
     * This should not be set unless the height of the tree actually matters.
     */
    fun node_depth_bound(): Int? = null

    /** Maximum allowed number of connected components. */
    fun max_n_components(): Int = 1
}
