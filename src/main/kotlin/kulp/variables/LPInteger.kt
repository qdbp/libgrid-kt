package kulp.variables

import kulp.BindCtx

/** A primitive LP integer without bounds */
context(BindCtx)
class LPInteger(lb: Int?, ub: Int?) : PrimitiveLPInteger(lb, ub)
