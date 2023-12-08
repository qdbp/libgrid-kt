package kulp

typealias LPObjective = Pair<LPAffExpr<*>, LPObjectiveSense>

/** The root renderable */
abstract class LPProblem private constructor(node: LPNode) : LPRenderable(node) {

    companion object {
        val null_objective: LPObjective = Pair(RealAffExpr(0.0), LPObjectiveSense.Minimize)
    }

    constructor() : this(LPNode.new_root()) {
        @Suppress("LeakingThis")
        node.renderable = this
    }

    /**
     * We don't separate variables from constraints here because these are often intimately coupled
     * through intermediate expressions. It would impose a lot of complexity for no good reason to
     * force implementors to separate these.
     */
    abstract fun get_objective(): LPObjective

    // we don't force problems to implement decompose, since it's assumed they're never primitive
    // and can just define their constituents as fields
    override fun decompose(ctx: LPContext) {}
}
