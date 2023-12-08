package kulp.variables

import kulp.LPNode

/** Base of the sealed primitive hierarchy for reals */
sealed class PrimitiveLPReal(
    node: LPNode,
    override val lb: Double? = null,
    override val ub: Double? = null
) : BaseLPReal(node)
