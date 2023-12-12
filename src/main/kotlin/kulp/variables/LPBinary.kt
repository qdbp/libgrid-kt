package kulp.variables

import kulp.BindCtx
import kulp.expressions.IntAffExpr

context(BindCtx)
class LPBinary : PrimitiveLPInteger(0, 1) {
    operator fun not(): IntAffExpr = IntAffExpr(mapOf(this.path to -1), 1)

    infix fun and(other: LPBinary): IntAffExpr =
        IntAffExpr(mapOf(this.path to 1, other.path to 1), -1)

    infix fun or(other: LPBinary): IntAffExpr =
        IntAffExpr(mapOf(this.path to 1, other.path to 1), 0)

    infix fun implies(other: LPBinary): IntAffExpr =
        IntAffExpr(mapOf(this.path to -1, other.path to 1), 0)

    companion object {
        /**
         * Makes a binary variable that is constrained to be always true
         *
         * These are useful as special simplified cases in operations that by contract must return a
         * named variable.
         */
        context(BindCtx)
        val true_pinned: LPVar<Int>
            get() {
                return LPBinary().requiring("always_true") { it eq 1}
            }

        /** Makes a binary variable that is constrained to be always false */
        context(BindCtx)
        val false_pinned: LPVar<Int>
            get() {
                return LPBinary().requiring("always_false") { it.eqz }
            }
    }
}
