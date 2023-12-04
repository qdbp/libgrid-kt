package grid_model.kulp_adapters

import grid_model.Tile
import kulp.*
import kulp.constraints.LP_GEQ
import kulp.constraints.LP_LEQ
import kulp.variables.LPBinary
import model.SegName

// TODO TEST ALL THIS

/** A constraint on the assignment of tiles at a single point. */
sealed interface PointTileConstraint : TileAssignmentPredicate {

    /**
     * Given a set of active tiles at a point, returns true if the constraint is satisfied.
     *
     * This is not useful for compiling to the LP formulation, but can be used to debug at a higher
     * level, e.g. with a pre-solver. This is also useful for forming visualizations.
     */
    fun is_satisfied_for(ctx: MipContext, tile_at_point: Set<Tile>): Boolean {
        return satisfaction_witness(ctx, tile_at_point.associateWith { LPBinary(it.tile_name()) })
            .evaluate(tile_at_point.associate { it.tile_name() to 1 }.withDefault { 0 })!! >= 1
    }
}

class IsOneOf(override val name: SegName, val required_tiles: Set<Tile>) : PointTileConstraint {
    /** Need to take a simple OR over all the assignments that are also in our desired set. */
    override fun satisfaction_witness(
        ctx: MipContext,
        assignment: Map<Tile, LPBinary>
    ): LPAffExpr<Int> {
        // starting out easy...
        return assignment.filterKeys { it in required_tiles }.values.lp_sum()
    }
}

class IsNoneOf(override val name: SegName, val tiles: Set<Tile>) : PointTileConstraint {
    override fun satisfaction_witness(
        ctx: MipContext,
        assignment: Map<Tile, LPBinary>
    ): LPAffExpr<Int> {
        val M = ctx.intM
        // we save an auxiliary by expressing this variable as the negative
        val z_bad = LPBinary(name.refine("z_bad_tile"))
        val constraints =
            assignment.flatMap { (t, x) ->
                listOf(
                    LP_GEQ(z_bad.name.refine("ge_${t.tile_name()}"), M * z_bad, x),
                    LP_LEQ(z_bad.name.refine("le_${t.tile_name()}}"), z_bad, x)
                )
            }
        return z_bad.requiring(constraints)
    }
}

class Xor(override val name: SegName, val constraints: Set<PointTileConstraint>) :
    PointTileConstraint {
    override fun satisfaction_witness(
        ctx: MipContext,
        assignment: Map<Tile, LPBinary>
    ): LPAffExpr<Int> {
        val clipped_constraints =
            constraints.map {
                it.satisfaction_witness(ctx, assignment).bool_clip(it.name.refine("xor_clip"))
            }
        // note, we CANNOT use LPOneOfN here, because that would FORCE the xor. We need to allow
        // a falsifiable expression.
        TODO()
    }
}

class Or(override val name: SegName, val constraints: Set<PointTileConstraint>) :
    PointTileConstraint {
    override fun satisfaction_witness(
        ctx: MipContext,
        assignment: Map<Tile, LPBinary>
    ): LPAffExpr<Int> {
        val clipped_constraints =
            constraints.map {
                it.satisfaction_witness(ctx, assignment).bool_clip(it.name.refine("or_clip"))
            }
        return clipped_constraints.lp_sum()
    }
}

class And(override val name: SegName, val constraints: Set<PointTileConstraint>) :
    PointTileConstraint {
    override fun satisfaction_witness(
        ctx: MipContext,
        assignment: Map<Tile, LPBinary>
    ): LPAffExpr<Int> {
        val clipped_constraints =
            constraints.map {
                it.satisfaction_witness(ctx, assignment).bool_clip(it.name.refine("and_clip"))
            }
        return clipped_constraints.lp_sum() - clipped_constraints.size + 1
    }
}

class Not(override val name: SegName, val constraint: PointTileConstraint) : PointTileConstraint {
    override fun satisfaction_witness(
        ctx: MipContext,
        assignment: Map<Tile, LPBinary>
    ): LPAffExpr<Int> {
        // TODO I'm starting to worry about variable discoverability through the expression tree...
        //  LPRenderable as it is might not be up to the task. Consider what would happen if I had
        //  made this !constriant(...).output, that would be a renderable that would miss all the
        //  auxiliaries of the constraint.
        return (1 - constraint.satisfaction_witness(ctx, assignment)).int_clip(name, 0, 1)
    }
}
