package kulp

abstract class LPProblem : LPRenderable {

    abstract fun get_objective(): Pair<LPAffExpr<*>, LPObjectiveSense>

    /**
     * We don't separate variables from constraints here because these are often intimately coupled
     * through intermediate expressions. It would impose a lot of complexity for no good reason to
     * force implementors to separate these.
     */
    abstract fun get_renderables(): List<LPRenderable>

    // The LP problem is where resolution into primitives happens.
    final override fun is_primitive(ctx: MipContext): Boolean = false

    final override fun render(ctx: MipContext): List<LPRenderable> {

        val resolved_renderables = mutableListOf<LPRenderable>()

        // copy these to mutable lists
        val open_renderables: MutableList<LPRenderable> = get_renderables().toMutableList()

        while (open_renderables.isNotEmpty()) {
            val renderable = open_renderables.removeFirst()
            if (renderable.is_primitive(ctx)) {
                resolved_renderables.add(renderable)
            } else {
                open_renderables.addAll(renderable.render(ctx))
            }
        }

        return resolved_renderables
    }
}
