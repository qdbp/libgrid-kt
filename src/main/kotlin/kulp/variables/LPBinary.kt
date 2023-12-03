package kulp.variables

import kulp.IntAffExpr
import model.SegName

class LPBinary(name: SegName) : LPInteger(name, 0, 1) {
    operator fun not(): IntAffExpr = IntAffExpr(mapOf(this.name to -1), 1)

    infix fun and(other: LPBinary): IntAffExpr =
        IntAffExpr(mapOf(this.name to 1, other.name to 1), -1)

    infix fun or(other: LPBinary): IntAffExpr =
        IntAffExpr(mapOf(this.name to 1, other.name to 1), 0)

    infix fun implies(other: LPBinary): IntAffExpr =
        IntAffExpr(mapOf(this.name to -1, other.name to 1), 0)
}
