package kulp.expressions

import kulp.domains.LPRealDomain

abstract class BaseLPRealExpr : LPSumExpr<Double>() {
    final override val dom = LPRealDomain
}