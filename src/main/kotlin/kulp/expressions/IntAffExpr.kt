package kulp.expressions

import kulp.LPPath
import kulp.is_nearly_int
import kulp.roundToInt
import requiring

data class IntAffExpr(override val terms: Map<LPPath, Int>, override val constant: Int) :
    BaseLPIntExpr() {

    constructor(
        constant: Number
    ) : this(mapOf(), constant.roundToInt() requiring constant.is_nearly_int())

}
