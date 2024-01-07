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
 * LPRenderable node for this reason, such as e.g. [kulp.aggregates.LPOneOfN], even if they
 * themselves do not "own" any variables or constraints.
 */
interface LPRenderable {

    val node: LPNode

    /**
     * If the given context doesn't support this renderable, this function will be called.
     *
     * The return value will be used as an immediate replacement of the decomposed node, with the
     * same path. Any expressions referring to this path will now refer to this substitute value.
     *
     * This function may also spawn sub-nodes which will in turn be recursively checked and
     * expanded.
     */
    context(NodeCtx)
    fun decompose(ctx: LPContext) {
        throw NotImplementedError(
            "${this.javaClass} was not primitive for $ctx, but did not implement decompose()"
        )
    }

    /**
     * If this value is to be decomposed by render, and this function returns not null, the returned
     * value will be substituted for this renderable in the rendered output. Otherwise, this
     * renderable, if compound, will be discarded.
     */
    fun as_primitive(ctx: LPContext): LPRenderable? = null

    val path: LPPath
        get() = node.path

}

/** Attaches new children to this node directly, also passing this object as a receiver.
 *
 * This method is very useful for testing and "editing" existing objects, but should be avoided
 * during routine tree construction */
infix fun <T : LPRenderable, V> T.use(op: context(NodeCtx, T) () -> V): V =
// need the `with` form surrounding the invoke to make sure that if we have some other node in
// the context (as we do with decompose), its String.invoke won't take precedence over the Ctx
    // invoke. This is the price of hipsterdom.
    with(node) { this { op(this, this@T) } }


infix fun <T : LPRenderable> T.branch(op: context(NodeCtx) (T) -> Unit): T {
    // need the `with` form surrounding the invoke to make sure that if we have some other node in
    // the context (as we do with decompose), its String.invoke won't take precedence over the Ctx
    // invoke. This is the price of hipsterdom.
    with(node) { this { op(this, this@T) } }
    return this
}


/**
 * The concrete implementation base class of LPRenderable.
 *
 * This is the final stop of the [LPNode.BindCtx].
 */
// due to various bugs with Kotlin with respect to using fields/methods of context objects
// in class constructors, we sometimes need to call `take()` in subclasses. For this reason the base
// class needs to handle both having a node passed (optionally), and taking a new node.
open class NodeBoundRenderable(final override val node: LPNode) : LPRenderable {

    class DuplicateNodeAssignmentException(node: LPNode) :
        IllegalStateException(
            "$node is unsuitable for a new renderable. " +
                    "It's either an invalid node, or has already been bound." +
                    "This probably means you passed the same node to multiple renderables." +
                    "You should use `node grow { ... }` or `node += ...` to split off new nodes."
        )

    init {
        // poor man's runtime simulation of linear types: each node is consumed is exactly once
        if (!node.is_uninitialized_data_node()) throw DuplicateNodeAssignmentException(node)
    }

    override fun toString(): String = javaClass.simpleName
}

open class RootRenderable(final override val node: LPNode) : LPRenderable {
    init {
        if (!node.is_root_node()) {
            throw IllegalStateException("Root renderable must be at a root node")
        }
    }
}
