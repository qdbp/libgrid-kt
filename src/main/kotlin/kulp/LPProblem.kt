package kulp

abstract class LPProblem : LPRenderable {

    // TODO might want to parameterize for CPSAT int-only objectives
    abstract fun get_objective(): Pair<LPAffExpr<*>, LPObjectiveSense>

    /**
     * We don't separate variables from constraints here because these are often intimately coupled
     * through intermediate expressions. It would impose a lot of complexity for no good reason to
     * force implementors to separate these.
     */
    abstract fun get_renderables(): List<LPRenderable>
}
