package kulp

import model.SegName

/**
 * A renderable is an entity (in practise, a variable or constraint) that ultimately has a
 * representation in the language of a MIP solver. Note, that this representation may be compound
 * (e.g. a variable may be represented as a sum of other variables with constraints).
 *
 * The `render` method returns a list of constituent renderables, which will be expanded recursively
 * until all renderables are primitive.
 *
 * A Renderable is primitive (with respect to a given context) if it is directly representable in
 * the language of the solver.
 */
interface LPRenderable {
    val name: SegName

    /**
     * Returns true if this renderable is primitive (i.e. will be handled directly by adapters).
     * Non-primitive renderables will be recursively rendered. Implementers must make sure that
     * this will eventually terminate.
     *
     * We pass the context because whether or not a renderable is primitive may depend on the
     * capabilities of the solver or of the computational environment, which are encapsulated
     * in the context.
     */
    fun is_primitive(ctx: MipContext): Boolean

    fun render(ctx: MipContext): List<LPRenderable>

}
