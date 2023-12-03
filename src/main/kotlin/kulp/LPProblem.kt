package kulp

import io.github.oshai.kotlinlogging.KotlinLogging
import model.SegName

private val logger = KotlinLogging.logger {}

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
        val seen_names = mutableSetOf<SegName>()

        // copy these to mutable lists
        val open_renderables: MutableList<LPRenderable> = get_renderables().toMutableList()

        while (open_renderables.isNotEmpty()) {
            val renderable = open_renderables.removeFirst()
            if (renderable.name in seen_names) {
                logger.warn { "Renderable ${renderable.name} already seen, skipping re-render." }
            }
            if (renderable.is_primitive(ctx)) {
                resolved_renderables.add(renderable)
                seen_names.add(renderable.name)
            } else {
                val new_renderables = renderable.render(ctx)
                open_renderables.addAll(new_renderables)
            }
        }

        return resolved_renderables
    }
}
