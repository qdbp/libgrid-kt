package kulp

/**
 * The fundamental class for a named "thing" in the LP model.
 *
 * The purpose of this class can be seen in two ways:
 *
 * From the mechanical perspective, this class is a handle on a named "thing" that lives in the
 * LPTree of a problem, serving as the data type of the tree.
 *
 * From a semantic perspective, this represents a possible decision point at which any given tree
 * processor (which will usually be the adapter that writes out the final problem) can ask, "should
 * I write this object as-is, or should I expand it into simpler objects."
 *
 * Any object that can *potentially* have representation as a primitive should be a named
 * LPRenderable node for this reason, such as e.g. [ConstrainedVariable], [LPOneOfN], even if they
 * themselves do not "own" any variables or constraints.
 */
open class LPRenderable(val node: LPNode) {

    class DuplicateNodeAssignmentException(node: LPNode) :
        IllegalStateException(
            "node ${node.full_path()} already has a renderable ${node.renderable}." +
                "This probably means you passed the same node to multiple renderables." +
                "You should use `node grow { ... }` or `node += ...` to split off new nodes."
        )

    init {
        // poor man's runtime simulation of linear types: each node is consumed is exactly once
        if (node.is_initialized()) throw DuplicateNodeAssignmentException(node)
    }

    /**
     * If this object is not deemed primitive for the given context, this method should expand its
     * node into a subtree of primitive objects, and return a Free<> over a residue object, if
     * any should remain, or null otherwise.
     *
     * Examples of residues:
     *  IntAbs -> LPInteger representing the abs value, with children + auxiliaries expanded.
     *  LP_EQ -> null, because the two LEZ children are the entirety of its meaning.
     *
     * This should be a deterministic and stateless operation.
     */
    open fun decompose(ctx: LPContext) {
        throw NotImplementedError(
            "${this.javaClass} was not primitive for $ctx, but did not implement decompose()"
        )
    }
}
