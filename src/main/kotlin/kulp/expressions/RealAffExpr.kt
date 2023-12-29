package kulp.expressions

import kulp.LPPath

/** Represents an affine expression with real coefficients, constants and variables. */
data class RealAffExpr(override val terms: Map<LPPath, Double>, override val constant: Double) :
    BaseLPRealExpr() {

    constructor(constant: Number) : this(mapOf(), constant.toDouble())

    constructor() : this(0.0)
}
