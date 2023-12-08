package kulp.variables

import kulp.LPNode

/** A primitive LP integer */
/** Base of the sealed primitive hierarchy for reals */
sealed class PrimitiveLPInteger(
    node: LPNode,
    override val lb: Int? = null,
    override val ub: Int? = null
) : BaseLPInteger(node)
