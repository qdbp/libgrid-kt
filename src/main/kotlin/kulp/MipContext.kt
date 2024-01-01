package kulp

import kulp.constraints.LP_LEZ
import kulp.variables.LiftedLPVar
import kulp.variables.PrimitiveLPInteger
import kulp.variables.PrimitiveLPReal
import kotlin.math.roundToInt

class UnsupportedRenderableError(msg: String) : IllegalArgumentException(msg)

enum class RenderSupport {
    PrimitiveVariable,
    PrimitiveConstraint,
    Compound,
    Unsupported;

}

sealed class LPContext {

    fun check_support(thing: LPRenderable): RenderSupport {
        return when (thing) {
            is PrimitiveLPInteger ->
                if (this is IntegerCapability) RenderSupport.PrimitiveVariable
                else RenderSupport.Unsupported
            is PrimitiveLPReal ->
                if (this is RealCapability) RenderSupport.PrimitiveVariable
                else RenderSupport.Unsupported
            is LP_LEZ -> RenderSupport.PrimitiveConstraint
            is LiftedLPVar<*> ->
                throw IllegalStateException("Failed to lower $thing during rendering.")
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
}

interface RealCapability : SolverCapability

interface IntegerCapability : SolverCapability

class MipContext(override val bigM: Double) :
    LPContext(), BigMCapability, RealCapability, IntegerCapability
