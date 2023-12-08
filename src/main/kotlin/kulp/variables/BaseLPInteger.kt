package kulp.variables

import kulp.Integral
import kulp.LPNode

// base class for variable-like integers
abstract class BaseLPInteger(node: LPNode) : LPVar<Int>(node, Integral)
