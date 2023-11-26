package kulp.aggregates

import kulp.LPName
import kulp.LPRenderable
import kulp.MipContext
import mdspan.MDSpan

abstract class  LPAggregate<T: LPRenderable>(override val name: LPName, vars: List<T>, shape: List<Int>): MDSpan<T>(vars, shape), LPRenderable {

    final override fun is_primitive(ctx: MipContext): Boolean = false

    final override fun render(ctx: MipContext): List<LPRenderable> {
        val out = this.map { it.render(ctx) }.flatten().toMutableList()
        out.addAll(render_interactions())
        return out
    }

    /**
     * Return a list of renderables needed to model the "interaction terms" of this aggregate.
     * When the aggregate is rendered, these are appended to the render output of the individual
     * constituent variables.
     *
     * If such a list is empty, you are probably better off using a simple container
     * instead of an LPAggregate subclass.
     */
    abstract fun render_interactions(): List<LPRenderable>
}