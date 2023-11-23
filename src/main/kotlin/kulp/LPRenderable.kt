package kulp

/**
 * Renders a maybe-complex constraint into a list of auxiliary variables and list of
 * simpler (though not necessarily primitive) constraints.
 */
interface LPRenderable {
    /**
     * Returns true if this renderable is primitive (i.e. will be handled directly by adapters).
     * Non-primitive renderables will be recursively rendered. Implementers must make sure that
     * this will eventually terminate.
     *
     * We pass the context because whether or not a renderable is primitive may depend on the
     * capabilities of the solver or of the computational environment, which are encapsulated
     * in the context.
     */
    val name: String

    fun is_primitive(ctx: MipContext): Boolean

    fun render(ctx: MipContext): List<LPRenderable>

}
