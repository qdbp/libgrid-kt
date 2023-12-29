package kulp.expressions

import kulp.LPPath
import kulp.is_nearly_int
import kulp.roundToInt

data class IntAffExpr(override val terms: Map<LPPath, Int>, override val constant: Int) :
    BaseLPIntExpr() {

    constructor(
        constant: Number
    ) : this(mapOf(), require(constant.is_nearly_int()).run { constant.roundToInt() })

    constructor() : this(0)
}
