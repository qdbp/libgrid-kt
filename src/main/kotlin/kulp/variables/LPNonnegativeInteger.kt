package kulp.variables

import kulp.BindCtx
import kulp.LPNode

context(BindCtx)
class LPNonnegativeInteger(name: LPNode) : PrimitiveLPInteger(0, null)
