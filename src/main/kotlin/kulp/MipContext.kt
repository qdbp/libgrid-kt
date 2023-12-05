package kulp

import kotlin.math.roundToInt
import kulp.constraints.LP_LEQ
import kulp.variables.LPInteger
import kulp.variables.LPReal

enum class RenderSupport {
    PrimitiveVariable,
    PrimitiveConstraint,
    Compound,
    Unsupported;

    fun can_render(): Boolean = this != Unsupported
}

sealed class LPContext {
    fun check_support(thing: LPRenderable): RenderSupport {
        return when (thing) {
            is LPInteger ->
                if (this is IntegerCapability) RenderSupport.PrimitiveVariable
                else RenderSupport.Unsupported
            is LPReal ->
                if (this is RealCapability) RenderSupport.PrimitiveVariable
                else RenderSupport.Unsupported
            is LP_LEQ -> RenderSupport.PrimitiveConstraint
            // TODO: for now we assume everything else is compound. We might want to explicitly
            //  reject some things explicitly
            else -> RenderSupport.Compound
        }
    }
}

interface SolverCapability

/** Contexts implementing this interface support bigM-style formulation */
interface BigMCapability : SolverCapability {
    val bigM: Double

    val intM: Int
        get() = bigM.roundToInt()
}

interface RealCapability : SolverCapability

interface IntegerCapability : SolverCapability

class LpContext() : LPContext(), RealCapability

class MipContext(override val bigM: Double = 1000.0) :
    LPContext(), BigMCapability, RealCapability, IntegerCapability

class CPSATContext() : LPContext(), IntegerCapability
