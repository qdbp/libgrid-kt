package kulp

const val LP_NAMESPACE_SEP = "|"

/**
 * A suggestive type alias that tells us that what we have is an unbound renderable that requires a
 * node to be allocated before it can be materialized.
 *
 * Most internal business logic will work with Frees where possible in a continuation-passing-like
 * style.
 */
typealias Free<T> = (LPNode) -> T

infix fun <T : LPRenderable> Free<T>.named(s: String): Attachable<T> {
    return object : Attachable<T> {
        override val name: String = s
        override val free: Free<T> = this@named
    }
}

infix fun <T : LPRenderable> Free<T>.named(s: List<Int>): Attachable<T> = this named s.lp_name

/**
 * A wrapping interface around, effectively, a complete named renderable that that requires an
 * [LPNode] to attach to [LPRenderable.decompose], this exposes a natural "builder" style syntax for
 * generating related lists of renderables.
 */
interface Attachable<out T : LPRenderable> {
    val name: String
    val free: Free<T>
}

/** This class manages the hierarchy of Renderable objects for a given task. */
class LPNode private constructor(val name: String, val parent: LPNode?) {

    val children: MutableMap<String, LPNode> = mutableMapOf()
    lateinit var renderable: LPRenderable

    fun is_initialized(): Boolean = this::renderable.isInitialized

    fun full_path(sep: String = LP_NAMESPACE_SEP): String {
        var out =
            when (parent) {
                null -> sep
                else -> parent.full_path() + sep + name
            }.replaceFirst("${sep}${sep}", sep)
        if (is_initialized()) {
            out += ":${renderable::class.simpleName}@${renderable.hashCode().toString(16)}"
        } else {
            out += ":?"
        }
        return out
    }

    /** Dumps a flat list of full paths of all nodes in the tree, in depth-first order. */
    fun dump_full_tree_dfs(): List<String> =
        children.values.flatMap { it.dump_full_tree_dfs() } + full_path()

    companion object {
        fun new_root(): LPNode = LPNode("", null)
    }

    /** danger zone! NEVER make this public. */
    private fun clone_subtree(): LPNode {
        val new_node = LPNode(name, null)
        new_node.renderable = renderable
        for ((k, v) in children) {
            new_node.children[k] = v.clone_subtree()
        }
        return new_node
    }

    private fun spawn_empty(name: String): LPNode {
        println("spawning $name in $this")
        val new_node = LPNode(name, this)
        if (children.containsKey(name)) {
            throw IllegalArgumentException("Child '$name' already exists in ${this.full_path()}")
        }
        children[name] = new_node
        return new_node
    }

    class Unnamed<out T : LPRenderable>(private val parent: LPNode, private val op: Free<T>) {
        infix fun named(s: String): T = parent grow (op named s)

        infix fun named(s: List<Int>): T = this named s.lp_name
    }

    /** Hipsters 1 : 0 Corporate * */
    // operator fun plusAssign(attachable: Attachable) = this + attachable.free named
    // attachable.name
    operator fun plusAssign(attachable: Attachable<*>) {
        grow(attachable) // and don't bother returning it
    }

    operator fun plusAssign(attachables: Iterable<Attachable<*>>) =
        attachables.forEach { this += it }

    /** Returns the realization of the attachable as a full LPRenderable, attached to this node. */
    infix fun <T : LPRenderable> grow(attachable: Attachable<T>): T {
        val new_node = spawn_empty(attachable.name)
        val renderable = attachable.free(new_node)
        new_node.renderable = renderable
        return renderable
    }

    infix fun <T : LPRenderable> grow(attachables: Iterable<Attachable<T>>) =
        attachables.forEach { this grow it }

    /**
     * infix hipsterdom for adding plain new variables
     *
     * node grow { LPInteger(it) } named "x"
     */
    infix fun <T : LPRenderable> grow(op: Free<T>): Unnamed<T> = Unnamed(this, op)

    /**
     * infix hipsterdom for adding plain new variables
     *
     * node { LPInteger(it) } named "x"
     */
    operator fun <T : LPRenderable> plus(op: Free<T>): Unnamed<T> = grow(op)

    /**
     * Flattens the tree, expanding any non-primitive nodes (according to the context), while adding
     * any primitive nodes as-is.
     *
     * This method might more naturally belong on [LPContext] itself, but it's in [LPNode] to keep
     * the internal structure of [LPNode] private. This is an overriding objective of the design.
     */
    fun render(ctx: LPContext): Map<LPNode, LPRenderable> {

        val open_set: MutableList<LPNode> = mutableListOf(this)
        val out_map: MutableMap<LPNode, LPRenderable> = mutableMapOf()

        while (open_set.isNotEmpty()) {
            val next = open_set.removeFirst()
            assert(next !in out_map)
            if (next.renderable.node !== next) {
                throw IllegalArgumentException(
                    "Renderable ${next.renderable} has a different node than $next (namely, ${next.renderable.node})"
                )
            }
            val support = ctx.check_support(next.renderable)
            println("checked $next support: $support")
            // we always add this renderable, even if it's not primitive. This is because, e.g.,
            // an IntAbs is still an Integer. Even if we skip expanding its dependents because
            // our hypothetical solver treats it as a primitive, we must still keep the node itself!
            out_map[next] = next.renderable
            when (support) {
                RenderSupport.PrimitiveVariable,
                RenderSupport.PrimitiveConstraint -> {}
                RenderSupport.Compound -> {
                    println("expanding $next")
                    next.renderable.decompose(ctx)
                }
                RenderSupport.Unsupported ->
                    throw IllegalArgumentException(
                        "Context $this considers this renderable to be unsupported."
                    )
            }
            open_set += next.children.values
        }

        return out_map
    }

    /**
     * Estimate the computational cost of adding this node to the model.
     *
     * Renderables will be expanded in a cloned tree, and will not affect the state of the original
     * tree.
     */
    fun subtree_cost(ctx: LPContext): Int? =
        try {
            clone_subtree().render(ctx).values.sumOf { ctx.estimate_primitive_cost(it) }
        } catch (e: UnsupportedRenderableError) {
            null
        }

    override fun toString(): String {
        // return "LPNode(${full_path()})@" + super.toString().split("@")[1]
        return "`${full_path()}`"
    }
}

val List<Int>.lp_name: String
    get() {
        return when (this.size) {
            1 -> this[0].toString()
            else -> "(${",".join(this)})" // includes 0, yes
        }
    }
