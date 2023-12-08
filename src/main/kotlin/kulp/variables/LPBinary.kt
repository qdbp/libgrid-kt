package kulp.variables

import kulp.Free
import kulp.IntAffExpr
import kulp.LPNode
import kulp.named

class LPBinary(node: LPNode) : PrimitiveLPInteger(node, 0, 1) {
    operator fun not(): IntAffExpr = IntAffExpr(mapOf(this.node to -1), 1)

    infix fun and(other: LPBinary): IntAffExpr =
        IntAffExpr(mapOf(this.node to 1, other.node to 1), -1)

    infix fun or(other: LPBinary): IntAffExpr =
        IntAffExpr(mapOf(this.node to 1, other.node to 1), 0)

    infix fun implies(other: LPBinary): IntAffExpr =
        IntAffExpr(mapOf(this.node to -1, other.node to 1), 0)

    companion object {
        /**
         * Makes a binary variable that is constrained to be always true
         *
         * These are useful as special simplified cases in operations that by contract must return a
         * named variable.
         */
        val true_pinned: Free<LPVar<Int>>
            get() {
                return { LPBinary(it) requiring { it eq 1 named "always_true" } }
            }

        /** Makes a binary variable that is constrained to be always false */
        val false_pinned: Free<LPVar<Int>>
            get() {
                return { LPBinary(it) requiring { it.eqz named "always_false" } }
            }
    }
}
