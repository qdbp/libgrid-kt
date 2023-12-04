package grid_model.kulp_adapters

import grid_model.Tile
import kulp.LPAffExpr
import kulp.MipContext
import kulp.constraints.LP_GEQ
import kulp.constraints.LP_LEQ
import kulp.variables.LPBinary
import model.SegName

/**
 * Objects satisfying this interface can produce a boolean predicate over some tiles variables,
 * given a mapping of those tiles to their underlying binary variables.
 */
// TODO merge with PointTileConstraint?? Who else would implement this?
sealed interface TileAssignmentPredicate {

    val name: SegName

    /**
     * A bridge between grid-world and LP world.
     *
     * Given a context and a mapping of tiles to their underlying binary variables, produce an
     * affine expression that sums to 1 or greater iff the predicate is satisfied.
     *
     * We keep the output format as "loose" as possible (i.e. do not require reification or a
     * logical clip), so that the caller may optimize the expression as necessary.
     *
     * Conversely, the caller must guarantee that the assignment targets are simple binary variables
     * -- through clipping and reification if necessary -- as supporting arbitrary expression
     * assignment would make this method extremely complicated to implement.
     *
     * NOTE: THE CONSTRUCTION OF THIS EXPRESSION SHOULD NOT CONSTRAIN THE UNDERLYING VARIABLES. THAT
     * WILL BREAK EVERYTHING. It should only be a "witness" expression, whose value depends on, but
     * does not impinge on, the underlying variables.
     *
     * If we want to *force* the constraint to be true, it should be enough to add the constraint
     * that the witness expression is greater than or equal to 1.
     */
    fun satisfaction_witness(ctx: MipContext, assignment: Map<Tile, LPBinary>): LPAffExpr<Int>

    fun forcing_constraint(ctx: MipContext, assignment: Map<Tile, LPBinary>): LP_LEQ {
        return LP_GEQ(name.refine("force"), satisfaction_witness(ctx, assignment), 1)
    }
}
