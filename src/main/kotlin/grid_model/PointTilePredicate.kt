//package grid_model
//
//import kulp.*
//import kulp.transforms.IntEQZWitness
//import kulp.variables.LPBinary
//import model.LPNode
//
//// TODO TEST ALL THIS
//
///** A constraint on the assignment of tiles at a single point. */
//sealed interface PointTilePredicate {}
//
///**
// * A "simple" tile predicate that can produce a witness to its satisfaction in the language of LP
// * modeling.
// *
// * Objects satisfying this interface can produce a boolean predicate over some tiles variables,
// * given a grid-provided mapping.
// *
// * We seal this class to minimize the surface of the tricky LP correctness code that is to be
// * tested. More advanced predicates should implement the [PointTilePredicate], trusting `simplify`
// * to bring them down to this level (eventually).
// */
//sealed class LPTilePredicate : LPRenderable {
//
//    /**
//     * A bridge between grid-world and LP world.
//     *
//     * Given a context and a mapping of tiles to their underlying binary variables, produce an
//     * affine expression that sums to 1 or greater iff the predicate is satisfied.
//     *
//     * We keep the output format as "loose" as possible (i.e. do not require reification or a
//     * logical clip), so that the caller may optimize the expression as necessary.
//     *
//     * Conversely, the caller must guarantee that the assignment targets are simple binary variables
//     * -- through clipping and reification if necessary -- as supporting arbitrary expression
//     * assignment would make this method extremely complicated to implement.
//     *
//     * NOTE: THE CONSTRUCTION OF THIS EXPRESSION SHOULD NOT CONSTRAIN THE UNDERLYING VARIABLES. THAT
//     * WILL BREAK EVERYTHING. It should only be a "witness" expression, whose value depends on, but
//     * does not impinge on, the underlying variables.
//     *
//     * If we want to *force* the constraint to be true, it should be enough to add the constraint
//     * that the witness expression is greater than or equal to 1.
//     */
//    abstract fun satisfaction_witness(
//        ctx: MipContext,
//        chart: GridChart<LPAffExpr<Int>>,
//        // TODO need a variable because we need this to be renderable
//    ): LPVariable<Int>
//
//    // TODO might want to allow overrides for more efficient impls than constraint on witness
//    //  however, for that we'll need to make this class LPRenderable to allow variable discovery
//    //  that will required Renderable reform
//    /**
//     * Constructs an LPConstraint, that, when added to the problem, will require that this tile
//     * predicate is satisfied.
//     */
//    fun forcing_constraint(ctx: MipContext, lp_chart: GridChart<LPAffExpr<Int>>): LPConstraint {
//        return LP_GEQ(name.refine("force"), satisfaction_witness(ctx, lp_chart), 1)
//    }
//
//    /**
//     * Given a set of active tiles at a point, returns true if the constraint is satisfied.
//     *
//     * This is not useful for compiling to the LP formulation, but can be used to debug at a higher
//     * level, e.g. with a pre-solver. This is also useful for forming visualizations.
//     */
//    fun is_satisfied_for(ctx: MipContext, tile_at_point: Set<Tile>): Boolean {
//        return satisfaction_witness(ctx, tile_at_point.associateWith { LPBinary(it.tile_name()) })
//            .evaluate(tile_at_point.associate { it.tile_name() to 1 }.withDefault { 0 })!! >= 1
//    }
//}
//
//class IsOneOf(override val name: LPNode, val required_tiles: Set<Tile>) :
//    LPTilePredicate(), PointTilePredicate {
//    /** Need to take a simple OR over all the assignments that are also in our desired set. */
//    override fun satisfaction_witness(
//        ctx: MipContext,
//        chart: GridChart<LPAffExpr<Int>>
//    ): LPVariable<Int> {
//        // starting out easy...
//        return mapping
//            .filterKeys { it in required_tiles }
//            .values
//            .lp_sum()
//            .reify(name.refine("is_one_of"))
//    }
//}
//
//class IsNoneOf(name: LPNode, val tiles: Set<Tile>) : LPTilePredicate(name) {
//    override fun satisfaction_witness(
//        ctx: MipContext,
//        mapping: Map<Tile, LPBinary>
//    ): LPVariable<Int> {
//        val M = ctx.intM
//        // we save an auxiliary by expressing this variable as the negative
//        val z_bad = LPBinary(name.refine("z_bad_tile"))
//        val constraints =
//            mapping.flatMap { (t, x) ->
//                listOf(
//                    LP_GEQ(z_bad.name.refine("ge_${t.tile_name()}"), M * z_bad, x),
//                    LP_LEQ(z_bad.name.refine("le_${t.tile_name()}}"), z_bad, x)
//                )
//            }
//        return z_bad.requiring(constraints)
//    }
//}
//
//class Xor(name: LPNode, val constraints: Set<LPTilePredicate>) : LPTilePredicate(name) {
//
//    // TODO lift these to ReifiedXYZ transforms to decouple them from the grid stuff
//    override fun satisfaction_witness(
//        ctx: MipContext,
//        mapping: Map<Tile, LPBinary>
//    ): LPVariable<Int> {
//        val clipped_constraints =
//            constraints.map {
//                it.satisfaction_witness(ctx, mapping).bool_clip(it.name.refine("xor_clip"))
//            }
//        // note, we CANNOT use LPOneOfN here, because that would FORCE the xor. We need to allow
//        // a falsifiable expression.
//        return IntEQZWitness(name.refine("xor"), IntAffExpr(1), clipped_constraints.lp_sum())
//    }
//}
//
//class Or(name: LPNode, val constraints: Set<LPTilePredicate>) : LPTilePredicate(name) {
//    override fun satisfaction_witness(
//        ctx: MipContext,
//        mapping: Map<Tile, LPBinary>
//    ): LPVariable<Int> {
//        val clipped_constraints =
//            constraints.map {
//                it.satisfaction_witness(ctx, mapping).bool_clip(it.name.refine("or_clip"))
//            }
//        return clipped_constraints.lp_sum().reify(name.refine("or"))
//    }
//}
//
//class And(name: LPNode, val constraints: Set<LPTilePredicate>) : LPTilePredicate(name) {
//    override fun satisfaction_witness(
//        ctx: MipContext,
//        mapping: Map<Tile, LPBinary>
//    ): LPVariable<Int> {
//        val clipped_constraints =
//            constraints.map {
//                it.satisfaction_witness(ctx, mapping).bool_clip(it.name.refine("and_clip"))
//            }
//        return (clipped_constraints.lp_sum() - clipped_constraints.size + 1).reify(
//            name.refine("and")
//        )
//    }
//}
//
//class Not(name: LPNode, val constraint: LPTilePredicate) : LPTilePredicate(name) {
//    override fun satisfaction_witness(
//        ctx: MipContext,
//        mapping: Map<Tile, LPBinary>
//    ): LPVariable<Int> {
//        // TODO I'm starting to worry about variable discoverability through the expression tree...
//        //  LPRenderable as it is might not be up to the task. Consider what would happen if I had
//        //  made this !constriant(...).output, that would be a renderable that would miss all the
//        //  auxiliaries of the constraint.
//        return (1 - constraint.satisfaction_witness(ctx, mapping)).int_clip(name, 0, 1)
//    }
//}
