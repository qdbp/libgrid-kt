package kulp.variables

import kulp.BindCtx

/** Unbounded primitive LP real. */
context(BindCtx)
class LPReal(lb: Double?, ub: Double?) : PrimitiveLPReal(lb, ub)
