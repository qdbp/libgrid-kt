package kulp.variables

import kulp.LPAffineExpression
import model.SegName

class LPBinary(name: SegName) : LPInteger(name, LPIntBound(0), LPIntBound(1)) {
    operator fun not(): LPAffineExpression {
        return LPAffineExpression(
            mapOf(
                this.name to -1.0,
            ),
            1.0
        )
    }

    fun and(other: LPBinary): LPAffineExpression {
        return LPAffineExpression(
            mapOf(
                this.name to 1.0,
                other.name to 1.0,
            ),
            -1.0
        )
    }

    fun or(other: LPBinary): LPAffineExpression {
        return LPAffineExpression(
            mapOf(
                this.name to 1.0,
                other.name to 1.0,
            ),
            0.0
        )
    }

    fun implies(other: LPBinary): LPAffineExpression {
        return LPAffineExpression(
            mapOf(
                this.name to -1.0,
                other.name to 1.0,
            ),
            0.0
        )
    }
}
