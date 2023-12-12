package kulp.variables

import kulp.BindCtx

/** A primitive LP integer */
/** Base of the sealed primitive hierarchy for reals */
context(BindCtx)
sealed class PrimitiveLPInteger(override val lb: Int? = null, override val ub: Int? = null) :
    BaseLPInteger() {

    override fun reify(): LPVar<Int> = this
}
