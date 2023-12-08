package kulp.variables

import kulp.LPNode
import kulp.Real

abstract class BaseLPReal(node: LPNode) : LPVar<Double>(node, Real)
