package kulp.variables

import kulp.IntAffExpr
import kulp.constraints.LP_EQZ
import kulp.transforms.Constrained
import model.LPName

class LPBinary(name: LPName) : LPInteger(name, 0, 1) {
    operator fun not(): IntAffExpr = IntAffExpr(mapOf(this.name to -1), 1)

    infix fun and(other: LPBinary): IntAffExpr =
        IntAffExpr(mapOf(this.name to 1, other.name to 1), -1)

    infix fun or(other: LPBinary): IntAffExpr =
        IntAffExpr(mapOf(this.name to 1, other.name to 1), 0)

    infix fun implies(other: LPBinary): IntAffExpr =
        IntAffExpr(mapOf(this.name to -1, other.name to 1), 0)

    companion object {
        /**
         * Makes a binary variable that is constrained to be always true
         *
         * These are useful as special simplified cases in operations that by contract must return a
         * named variable.
         */
        fun mk_true_pinned(name: LPName): Constrained<Int> {
            return LPBinary(name) requiring { LP_EQZ(name.refine("always_true"), it - 1) }
        }

        /** Makes a binary variable that is constrained to be always false */
        fun mk_false_pinned(name: LPName): Constrained<Int> {
            return LPBinary(name) requiring { LP_EQZ(name.refine("always_false"), it) }
        }
    }
}
