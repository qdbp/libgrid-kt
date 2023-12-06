package kulp

import kulp.constraints.LP_LEZ
import kulp.variables.LPInteger
import kulp.variables.LPReal
import model.LPName
import kotlin.math.roundToInt

enum class RenderSupport {
    PrimitiveVariable,
    PrimitiveConstraint,
    Compound,
    Unsupported;

    fun can_render(): Boolean = this != Unsupported
}

sealed class LPContext {

    /**
     * Given some renderable, estimate the computational cost of adding it to the model.
     *
     * It's understood that, knowing nothing about the problem, this can be at best a very crude
     * guess. However, a decent implementation of this will allow higher-level modeling classes to
     * potentially choose one of many possible renderings of a given expression to minimize cost.
     *
     * By default, just prices each primitive renderable at 1.
     */
    open fun estimate_primitive_cost(thing: LPRenderable): Int {
        return 1
    }

    /** Given some renderable, estimate the computational cost of adding it to the model. */
    fun total_cost(thing: LPRenderable): Int {
        return render(thing).values.sumOf { estimate_primitive_cost(it) }
    }

    fun check_support(thing: LPRenderable): RenderSupport {
        return when (thing) {
            is LPInteger ->
                if (this is IntegerCapability) RenderSupport.PrimitiveVariable
                else RenderSupport.Unsupported
            is LPReal ->
                if (this is RealCapability) RenderSupport.PrimitiveVariable
                else RenderSupport.Unsupported
            is LP_LEZ -> RenderSupport.PrimitiveConstraint
            // TODO: for now we assume everything else is compound. We might want to explicitly
            //  reject some things explicitly
            else -> RenderSupport.Compound
        }
    }

    fun render(thing: LPRenderable): Map<LPName, LPRenderable> {

        val open_set = mutableListOf(thing)
        val out_map: MutableMap<LPName, LPRenderable> = mutableMapOf()

        while (open_set.isNotEmpty()) {
            val next = open_set.removeFirst()
            if (next.name in out_map) {
                throw IllegalArgumentException(
                    "Renderable ${next.name} already seen, skipping re-render."
                )
            }

            when (check_support(next)) {
                RenderSupport.PrimitiveVariable,
                RenderSupport.PrimitiveConstraint -> out_map[next.name] = next
                RenderSupport.Compound ->
                    with(next.name) { with(next) { open_set += decompose(this@LPContext) } }
                RenderSupport.Unsupported ->
                    throw IllegalArgumentException(
                        "Context $this considers this renderable to be unsupported."
                    )
            }
        }

        return out_map
    }

    /**
     * Goes through the declared renderables and expands them down to primitives which are
     * considered primitive by the context.
     *
     * Will raise if it encounters any renderables which the context claims it cannot process.
     */
    fun render(problem: LPProblem): Map<LPName, LPRenderable> {

        val out: MutableMap<LPName, LPRenderable> = mutableMapOf()

        for (root_renderable in problem.get_renderables()) {
            val rendered = render(root_renderable)
            if (rendered.keys.any { it in out.keys }) {
                throw IllegalArgumentException(
                    "Renderable ${root_renderable.name} has a name collision with a previously rendered " +
                        "renderable."
                )
            }
            out.putAll(rendered)
        }
        return out
    }
}

interface SolverCapability

/** Contexts implementing this interface support bigM-style formulation */
interface BigMCapability : SolverCapability {
    val bigM: Double

    val intM: Int
        get() = bigM.roundToInt()

    @Suppress("UNCHECKED_CAST")
    fun <N: Number> getM(info: ReifiedNumberTypeWrapper<N>): N = when (info) {
        IntWrapper -> intM
        DoubleWrapper -> bigM
        else -> throw IllegalArgumentException("Unsupported number type $info")
    } as N
}

interface RealCapability : SolverCapability

interface IntegerCapability : SolverCapability

class LpContext() : LPContext(), RealCapability

class MipContext(override val bigM: Double = 1000.0) :
    LPContext(), BigMCapability, RealCapability, IntegerCapability

class CPSATContext() : LPContext(), IntegerCapability
