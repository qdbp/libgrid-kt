package grid_model.kulp_adapters

import grid_model.Tile
import kulp.MipContext
import kulp.variables.LPBinary
import kulp.variables.LPVariable

/**
 * Objects satisfying this interface can produce a boolean predicate over some tiles variables,
 * given a mapping of those tiles to their underlying binary variables.
 */
sealed interface TileAssignmentPredicate {

    fun render_with_assignment(
        ctx: MipContext,
        assignment: Map<Tile, LPBinary>
    ): List<LPVariable<Int>> = TODO()

}
