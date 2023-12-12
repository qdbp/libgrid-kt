package kulp

import kotlin.math.roundToInt
import kulp.constraints.LP_LEZ
import kulp.domains.Integral
import kulp.domains.LPDomainImpl
import kulp.domains.Real
import kulp.variables.PrimitiveLPInteger
import kulp.variables.PrimitiveLPReal

class UnsupportedRenderableError(msg: String) : IllegalArgumentException(msg)

enum class RenderSupport {
    PrimitiveVariable,
    PrimitiveConstraint,
    Compound,
    Unsupported;

    fun can_render(): Boolean = this != Unsupported

    fun should_expand(): Boolean = this == Compound
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

    fun check_support(thing: LPRenderable): RenderSupport {
        return when (thing) {
            is PrimitiveLPInteger ->
                if (this is IntegerCapability) RenderSupport.PrimitiveVariable
                else RenderSupport.Unsupported
            is PrimitiveLPReal ->
                if (this is RealCapability) RenderSupport.PrimitiveVariable
                else RenderSupport.Unsupported
            is LP_LEZ -> RenderSupport.PrimitiveConstraint
            // TODO: for now we assume everything else is compound. We might want to explicitly
            //  reject some things explicitly
            else -> RenderSupport.Compound
        }
    }
}

sealed interface SolverCapability

/** Contexts implementing this interface support bigM-style formulation */
interface BigMCapability : SolverCapability {
    val bigM: Double

    val intM: Int
        get() = bigM.roundToInt()

    @Suppress("UNCHECKED_CAST")
    fun <N : Number> getM(domain: LPDomainImpl<N>): N =
        when (domain) {
            Integral -> intM
            Real -> bigM
        }
            as N
}

interface RealCapability : SolverCapability

interface IntegerCapability : SolverCapability

class LpContext : LPContext(), RealCapability

class MipContext(override val bigM: Double = 1000.0) :
    LPContext(), BigMCapability, RealCapability, IntegerCapability

class CPSATContext : LPContext(), IntegerCapability
