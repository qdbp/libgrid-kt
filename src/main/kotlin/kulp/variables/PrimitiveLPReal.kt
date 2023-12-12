package kulp.variables

import kulp.BindCtx

/** Base of the sealed primitive hierarchy for reals */
context(BindCtx)
sealed class PrimitiveLPReal(override val lb: Double? = null, override val ub: Double? = null) :
    BaseLPReal() {

    override fun reify(): LPVar<Double> = this
}
