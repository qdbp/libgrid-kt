package kulp

import kulp.expressions.RealAffExpr

typealias LPObjective = Pair<LPAffExpr<*>, LPObjectiveSense>

/** The root renderable */
abstract class LPProblem private constructor(node: LPNode) : RootRenderable(node) {

    companion object {
        val null_objective: LPObjective = Pair(RealAffExpr(0.0), LPObjectiveSense.Minimize)
    }

    constructor() : this(LPNode.new_root())

    /**
     * We don't separate variables from constraints here because these are often intimately coupled
     * through intermediate expressions. It would impose a lot of complexity for no good reason to
     * force implementors to separate these.
     */
    abstract fun get_objective(): LPObjective

    // we don't force problems to implement decompose, since it's assumed they're never primitive
    // and can just define their constituents as fields
    context(NodeCtx)
    override fun decompose(ctx: LPContext) {}

    // KT-64635
    // operator fun <T : LPRenderable> ((BindCtx).() -> T).provideDelegate(
    //     thisRef: Any?,
    //     prop: KProperty<*>
    // ): ReadOnlyProperty<Any?, T> {
    //     val out = node.bind(prop.name, this)
    //     return ReadOnlyProperty { _, _ -> out }
    // }

    fun <T : LPRenderable> bind(op: (BindCtx).() -> T): LPNode.BDP<T> = node.bind(op)
}
