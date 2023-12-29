package kulp.expressions

import kulp.domains.LPIntegralDomain

abstract class BaseLPIntExpr : LPSumExpr<Int>() {
    override val dom = LPIntegralDomain
}