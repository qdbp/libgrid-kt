package kulp

import model.LPName

/**
 * Base class for LP objects that can be referred to by name.
 *
 * Each such object should have a name representable in the underlying model, as well as implement a
 * predicate which is true if this object is "primitive" with respect to a given mode of
 * computation.
 */
interface LPRenderable {
    /**
     * The partial name of this object.
     *
     * We reserve the term "name" to refer to a fully qualified name (i.e. a path from the root of
     * the model to this object). Stems will not be unique, but names will be.
     */
    val name: LPName

    /**
     * If this object is not deemed primitive for the given context, this method should return a
     * decomposition of this object into primitive objects.
     *
     * The parent object's name will be made available as the receiver of this method for convenient
     * sub-name formation.
     */
    fun LPName.decompose(ctx: MipContext): List<LPRenderable> {
        throw NotImplementedError(
            "${this.javaClass} was not primitive for $ctx, but did not implement decompose()"
        )
    }
}
