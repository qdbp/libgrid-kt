package kulp.variables

import kulp.BindCtx
import kulp.LPNode

class LPNonnegativeReal private constructor(node: LPNode) : PrimitiveLPReal(node, 0.0, null) {
    companion object {
        context(BindCtx)
        operator fun invoke(): LPNonnegativeReal = LPNonnegativeReal(take())
    }
}
