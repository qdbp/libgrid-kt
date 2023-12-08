package kulp.variables

import kulp.LPNode

/** A primitive LP integer without bounds */
class LPInteger(node: LPNode, lb: Int?, ub: Int?) : PrimitiveLPInteger(node, lb, ub)
