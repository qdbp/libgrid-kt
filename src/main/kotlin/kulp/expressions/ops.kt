package kulp.expressions

import kulp.BindCtx
import kulp.LPAffExpr
import kulp.LPConstraint
import kulp.transforms.IntClip
import kulp.variables.LPVar

// extensions on LPAffExpr<Int> to be more generic
context(BindCtx)
fun LPAffExpr<Int>.int_clip(lb: Int? = null, ub: Int? = null): LPVar<Int> =
    IntClip.clip(this, lb, ub)

context(BindCtx)
fun LPAffExpr<Int>.bool_clip(): LPVar<Int> = int_clip(0, 1)

// int expressions support strict inequality
context(BindCtx)
infix fun LPAffExpr<Int>.lt(other: LPAffExpr<Int>): LPConstraint {
    return this le (other - 1)
}

context(BindCtx)
infix fun LPAffExpr<Int>.lt(other: Int): LPConstraint {
    return this le (other - 1)
}

context(BindCtx)
infix fun LPAffExpr<Int>.gt(other: LPAffExpr<Int>): LPConstraint = this ge (other + 1)

context(BindCtx)
infix fun LPAffExpr<Int>.gt(other: Int): LPConstraint = this ge (other + 1)

context(BindCtx)
fun LPAffExpr<Int>.ltz(): LPConstraint = this le -1

context(BindCtx)
fun LPAffExpr<Int>.gtz(): LPConstraint = this ge 1
