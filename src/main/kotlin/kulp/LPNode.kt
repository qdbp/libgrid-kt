package kulp

import kulp.variables.LPBinary
import kulp.variables.LPVar
import kulp.variables.LiftedLPVar
import mdspan.NDSpan
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

const val LP_NAMESPACE_SEP = "."

private data object Private

/**
 * A suggestive type alias that tells us that what we have is an unbound renderable that requires a
 * node to be allocated before it can be materialized.
 *
 * Most internal business logic will work with Frees where possible in a continuation-passing-like
 * style.
 */

// TODO we're reinventing monads or something... with type erasure lol... try another language???

typealias NodeCtx = LPNode.NodeCtx

typealias BindCtx = LPNode.BindCtx

class LPPath(val segments: List<String>) {
    fun render(sep: String = LP_NAMESPACE_SEP): String = segments.joinToString(sep)

    override fun toString(): String = render()

    override fun equals(other: Any?): Boolean {
        if (other !is LPPath) return false
        return this.segments == other.segments
    }

    override fun hashCode(): Int = segments.hashCode()
}

/** This class manages the hierarchy of Renderable objects for a given task. */
class LPNode
private constructor(val name: String, private val parent: LPNode?, private var data: NodeData) {

    // NODE TYPE
    private sealed class NodeData

    private object Root : NodeData() {
        override fun toString(): String = "[Root]"
    }

    private object UninitializedData : NodeData() {
        override fun toString(): String = "[Uninitialized]"
    }

    private object Structural : NodeData() {
        override fun toString(): String = "[Structural]"
    }

    private data class Data(val rnd: LPRenderable) : NodeData() {
        override fun toString(): String = rnd.toString()
    }

    fun is_uninitialized_data_node(): Boolean = this.data is UninitializedData

    fun is_root_node(): Boolean = this.data is Root

    val renderable: LPRenderable
        get() {
            (this.data).let {
                when (it) {
                    is Data -> return it.rnd
                    else -> throw IllegalStateException("Node $this has no renderable")
                }
            }
        }

    private val children: MutableMap<String, LPNode> = mutableMapOf()

    private val root: LPNode
        get() = parent?.root ?: this

    private fun find_rel(path: LPPath): LPNode? {
        var cur: LPNode = this
        for (segment in path.segments) {
            cur = cur.children[segment] ?: return null
        }
        return cur
    }

    fun find_var(path: LPPath): LPVar<*>? {
        return find(path)?.let {
            if (it.data !is Data) return null
            return when (it.renderable) {
                is LPVar<*> -> it.renderable as LPVar<*>
                is LiftedLPVar<*> -> (it.renderable as LiftedLPVar<*>).lower()
                else -> null
            }
        }
    }

    fun find(path: LPPath): LPNode? = root.find_rel(path)

    companion object {
        fun new_root(): LPNode {
            val root = LPNode("root", null, Root)
            root.data = Root
            return root
        }
    }

    /** danger zone! NEVER make this public. */
    private fun clone_subnode(): LPNode {
        val new_node = LPNode(name, null, Root)
        new_node.data = data
        for ((k, v) in children) {
            new_node.children[k] = v.clone_subnode()
        }
        return new_node
    }

    /** Internal function for attaching a node to this one. */
    private fun raw_attach(node: LPNode) {
        if (children.containsKey(node.name)) {
            throw IllegalArgumentException("Child '${node.name}' already exists in ${this.path}")
        }
        children[node.name] = node
    }

    /** Spawns an uninitialized child of this node and attaches it to its parent. */
    private fun new_child(name: String, init_data: NodeData): LPNode =
        LPNode(name, this, init_data).also { raw_attach(it) }

    private fun new_anon_name(): String = "_${children.size}"

    /** Removes this node from the tree. */
    private fun detach() {
        parent?.children?.remove(this.name)
    }

    @DslMarker private annotation class NodeContextMarker

    /**
     * A Node Context is the building block of the node builder pattern. One can think of it as a
     * type-safe, declaration of intent regarding how a new will be consumed.
     *
     * The LPTree is in charge of producing these contexts in a safe way, and consumers declare the
     * level of context they need.
     */
    @NodeContextMarker
    open class NodeCtx(protected val node: LPNode) {

        // we expose the expansion functions on the context directly for convenience, since
        // we have hidden the node.
        fun <T : LPRenderable> bind(name: String, op: (BindCtx).() -> T): T = node.bind(name, op)

        infix fun <T : LPRenderable> bind(op: (BindCtx).() -> T) = Private.run { BDP(node, op) }

        infix fun <T> branch(op: (NodeCtx).() -> T): T = node.branch(op)

        fun <T> branch(name: String, op: (NodeCtx).() -> T): T = node.branch(name, op)

        fun String.new_binary(): LPBinary = node.bind(this) { LPBinary() }

        // some infix hipster sugar
        operator fun <T : LPRenderable> String.invoke(op: (BindCtx).() -> T): T =
            node.bind(this, op)

        fun List<Int>.new_binary(): LPBinary = node.bind(lp_name) { LPBinary() }

        operator fun <T : LPRenderable> List<Int>.invoke(op: (BindCtx).() -> T): T =
            node.bind(lp_name, op)

        fun <T : LPRenderable, V> List<V>.bind_each(base: String, op: (BindCtx).(V) -> T): List<T> =
            when (this) {
                is NDSpan ->
                    this.mapNdIndexed { ndix, v -> bind("${base}${ndix.lp_name}") { op(v) } }
                else -> this.mapIndexed { ix, v -> bind("${base}_${ix}") { op(v) } }
            }

        fun <T, V> List<T>.branch_each(op: (NodeCtx).(T) -> V): List<V> =
            when (this) {
                is NDSpan -> this.mapNdIndexed { ndix, v -> branch(ndix.lp_name) { op(v) } }
                else -> this.mapIndexed { ix, v -> branch("$ix") { op(v) } }
            }

        fun <T, V> List<T>.branch_each(name: String, op: (NodeCtx).(T) -> V): List<V> =
            when (this) {
                is NDSpan ->
                    this.mapNdIndexed { ndix, v -> branch("${name}${ndix.lp_name}") { op(v) } }
                else -> this.mapIndexed { ix, v -> branch("${name}_${ix}") { op(v) } }
            }

        val root
            get() = node.root
    }

    open class BindCtx(node: LPNode) : NodeCtx(node) {
        init {
            require(node.data is UninitializedData)
        }

        private var used_name: Boolean = false

        fun used_name(): Boolean = used_name

        private var have_claimed = false

        fun have_claimed(): Boolean = have_claimed

        /**
         * Take the node from the binding context. Exactly one take() must be issued per bind()
         *
         * We, unfortunately, cannot enforce this through the type system, but effort is made to
         * detect violations during runtime as early as possible.
         */
        fun take(): LPNode {
            if (have_claimed) {
                throw IllegalStateException("Node $node has already been consumed")
            }
            have_claimed = true
            return node
        }

        // it's not actually unsafe, but this really only needs to be used in two
        // very technical places, so we maximally uglify it and give it a scary name.
        // The way this would be bad is if we use this path and then do not `take()` the node,
        // letting it revert to structural.
        val unsafe_path_of_new_node: LPPath
            get() {
                used_name = true
                return node.path
            }
    }

    /**
     * Opens a "binding" node context, which expects exactly at most one renderable to be attached
     * to it when all is said is done. The function producing the renderable may expand this node
     * arbitrarily in turn.
     *
     * This context simulates a linear type, where the node must be consumed exactly once by a new
     * renderable to become its node member.
     *
     * Spawns a new child, and wraps it in a [BindCtx]. This node must be claimed and passed to a
     * new renderable AT MOST ONCE by invocation of [BindCtx.take]. As with all growing methods, the
     * tree may be further expanded arbitrarily.
     *
     * If it is not bound to a new renderable, the spawned node will be downgraded to a structural
     * node. As a rule, a binding context should be requested unless you are intending to produce a
     * new renderable. The degradation to structural is required for a few "polymorphic" functions
     * (see [LPAffExpr.reify]) which cannot know in advance if a new renderable will actually be
     * produced.
     */
    fun <T : LPRenderable> bind(name: String, op: (BindCtx).() -> T): T {
        val new_node = new_child(name, UninitializedData)
        with(BindCtx(new_node)) {
            val renderable = op()
            if (!have_claimed()) {
                if (renderable.node === new_node) {
                    // TODO might be unreachable
                    throw IllegalStateException(
                        "Renderable $renderable did not claim its node but got it anyway??"
                    )
                }
                if (used_name()) {
                    throw IllegalStateException(
                        "Accessed path of node $new_node without claiming it. Likely broken."
                    )
                }
                // it's legal to fail to claim in a binding context -- the node is implicitly
                // downgraded to Structural. This is useful for "maybe binding" operations such
                // as reify (which binds on general expressions but is a nop on variables).
                new_node.data = Structural
            } else {
                when (renderable) {
                    is LiftedLPVar<*> -> new_node.data = Data(renderable.lower())
                    else -> new_node.data = Data(renderable)
                }
            }
            return renderable
        }
    }

    // hipster delegate sugar
    /**
     * Provides binding through a delegate factory. Allows to derive the renderable name from the
     * property name, avoiding needing to type the two out twice.
     */
    infix fun <T : LPRenderable> bind(op: (BindCtx).() -> T) = Private.run { BDP(this@LPNode, op) }

    context(Private)
    class BDP<T : LPRenderable>(private val parent: LPNode, val op: (BindCtx).() -> T) {
        operator fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, T> {
            val out = parent.bind(prop.name, op)
            return ReadOnlyProperty { _, _ -> out }
        }
    }

    /**
     * Spawns a structural node and runs some tree-building lambda in its context.
     *
     * Structural nodes do not have any data and serve as "namespacers" for the tree. They are
     * generally used to perform array-wide operations.
     */
    fun <T> branch(name: String, op: (NodeCtx).() -> T): T {
        val new_child = new_child(name, Structural)
        with(NodeCtx(new_child)) {
            return op().also {
                // prune dead leaf nodes. These will pile up quickly in e.g. RIL compile where
                // most "may use this node" operations actually don't.
                if (new_child.children.isEmpty()) {
                    new_child.detach()
                }
            }
        }
    }

    infix fun <T> branch(op: (NodeCtx).() -> T): T = branch(new_anon_name(), op)

    /** Opens a node context against this node, without spawning an intermediate child. */
    operator fun <T> invoke(op: (NodeCtx).() -> T): T = NodeCtx(this).run(op)

    /**
     * Flattens the tree, expanding any non-primitive nodes (according to the context), while adding
     * any primitive nodes as-is.
     *
     * This method might more naturally belong on [LPContext] itself, but it's in [LPNode] to keep
     * the internal structure of [LPNode] private. This is an overriding objective of the design.
     */
    fun render(ctx: LPContext): Map<LPPath, LPRenderable> {

        val open_set: MutableList<LPNode> = mutableListOf(this)
        val out_map: MutableMap<LPPath, LPRenderable> = mutableMapOf()
        var seen_root = false

        while (open_set.isNotEmpty()) {
            val next = open_set.removeFirst()
            val path = next.path
            assert(path !in out_map)

            (next.data).let { nd ->
                when (nd) {
                    is Data -> {
                        if (nd.rnd.node !== next) {
                            throw IllegalArgumentException(
                                "Renderable ${next.renderable} has a different node than $next " +
                                    "(namely, ${nd.rnd.node})"
                            )
                        }
                        val support = ctx.check_support(nd.rnd)
                        when (support) {
                            RenderSupport.PrimitiveVariable,
                            RenderSupport.PrimitiveConstraint -> {
                                out_map[path] = nd.rnd
                            }
                            RenderSupport.Compound -> {
                                next { nd.rnd.decompose(ctx) }
                                nd.rnd.as_primitive(ctx)?.let { out_map[path] = it }
                            }
                            RenderSupport.Unsupported ->
                                throw IllegalArgumentException(
                                    "Context $this considers this renderable to be unsupported."
                                )
                        }
                    }
                    // structural nodes are "pass through", we just expand the children
                    is Structural -> {}
                    // expression nodes are also pass-through, since they are not renderable.
                    // we store their data to be able to get their value from the solver later.
                    is Root -> {
                        if (!seen_root) seen_root = true
                        else throw IllegalStateException("Encountered multiple root nodes")
                    }
                    is UninitializedData -> {
                        throw IllegalStateException("Encountered uninitialized node $next")
                    }
                }
            }
            open_set += next.children.values
        }

        return out_map
    }

    val path: LPPath
        get() {
            val out = mutableListOf<String>()
            var cur: LPNode = this
            while (cur.parent != null) {
                out.add(
                    when (this.data) {
                        is Root -> ""
                        else -> cur.name
                    }
                )
                cur.parent?.let { cur = it } ?: break
            }
            return LPPath(out.reversed())
        }

    /** Dumps a flat list of full paths of all nodes in the tree, in depth-first order. */
    fun dump_full_node_dfs(indent: Int = 0) {
        println("    ".repeat(indent) + "$data @ $path")
        for (c in children.values) c.dump_full_node_dfs(indent + 1)
    }

    override fun toString(): String {
        return super.toString() + path + ":" + data
    }
}

val List<Int>.lp_name: String
    get() {
        return when (this.size) {
            1 -> this[0].toString()
            else -> "(${",".join(this)})" // includes 0, yes
        }
    }
