package grid_model

import kulp.LPProblem

/**
 * Three dogmas to keep in mind: 1 discreteness: each tile, for each plane, is either occupied or
 * not. 2 space locality and translation invariance: each entity knows nothing about the size of the
 * plane, and all of its extents are identical irrespective of where the entity is. 3 plane
 * locality: each entity knows a fixed set of planes and their associated tile sets. Adding
 * additional planes to the problem cannot change an entity's semantics or behavior.
 */
abstract class GridProblem() {

    abstract val name: String

    /** It's an ordered entity set, yes. */
    abstract fun get_entity_set(): Set<Entity>

    abstract fun get_shape(): List<Int>

    fun to_lp_problem(): LPProblem {
        TODO()
    }
}
