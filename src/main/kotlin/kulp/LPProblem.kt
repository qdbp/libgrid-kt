package kulp

import io.github.oshai.kotlinlogging.KotlinLogging
import java.lang.IllegalArgumentException
import model.LPName

private val logger = KotlinLogging.logger {}

abstract class LPProblem: LPRenderable {

    abstract fun get_objective(): Pair<LPAffExpr<*>, LPObjectiveSense>

    /**
     * We don't separate variables from constraints here because these are often intimately coupled
     * through intermediate expressions. It would impose a lot of complexity for no good reason to
     * force implementors to separate these.
     */
    abstract fun get_renderables(): List<LPRenderable>

    /**
     * Goes through the declared renderables and expands them down to primitives which are
     * considered primitive by the context.
     *
     * Will raise if it encounters any renderables which the context claims it cannot process.
     */
    fun render(ctx: MipContext): List<LPRenderable> {

        val resolved_renderables = mutableListOf<LPRenderable>()
        val seen_names = mutableSetOf<LPName>()

        // copy these to mutable lists
        val open_renderables: MutableList<LPRenderable> = get_renderables().toMutableList()

        while (open_renderables.isNotEmpty()) {
            val next = open_renderables.removeFirst()
            if (next.name in seen_names) {
                logger.warn { "Renderable ${next.name} already seen, skipping re-render." }
            }

            when (ctx.check_support(next)) {
                RenderSupport.PrimitiveVariable,
                RenderSupport.PrimitiveConstraint -> resolved_renderables.add(next)
                RenderSupport.Compound ->
                    with(next.name) { with(next) { open_renderables += decompose(ctx) } }
                RenderSupport.Unsupported ->
                    throw IllegalArgumentException(
                        "Context $ctx considers this renderable to be unsupported."
                    )
            }
        }

        return resolved_renderables
    }
}
