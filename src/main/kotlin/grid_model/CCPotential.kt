package grid_model

/**
 * Connected-Component Potential.
 *
 * A MIP formulation of the notion of having entities required to form a connected graph.
 */
interface CCPotential<T: Tile> {

    fun tiles_with_potential(): Set<T>

}
