package kulp

import mdspan.NDSpan
import model.LPName

/**
 * A rigidly-shaped, homogeneous collection of LPNamed objects.
 *
 * In contrast to the simple container, this cannot be added to after construction.
 *
 * This container is designed to support statically known interactions between its variables that
 * work for any underlying shape.
 */
abstract class LPAggregate<T : LPRenderable>(
    override val name: LPName,
    vars: List<T>,
    shape: List<Int>
) : NDSpan<T>(vars, shape), LPRenderable {

    final override fun LPName.decompose(ctx: MipContext): List<LPRenderable> {
        val base_renderables: List<T> = (this@LPAggregate).toList()
        return base_renderables + with(name) { render_interactions() }
    }

    /**
     * Return a list of renderables needed to model the "interaction terms" of this aggregate. When
     * the aggregate is rendered, these are appended to the render output of the individual
     * constituent variables.
     *
     * If such a list is empty, you are probably better off using a simple container instead of an
     * LPAggregate subclass.
     */
    abstract fun LPName.render_interactions(): List<LPRenderable>
}
