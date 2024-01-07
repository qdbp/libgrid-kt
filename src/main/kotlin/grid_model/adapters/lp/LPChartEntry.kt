package grid_model.adapters.lp

import kulp.expressions.LPBinaryExpr
import kulp.expressions.One
import kulp.expressions.Zero

/**
 * Thin wrapper around [LPBinaryExpr] that supports an abstraction notion of "Masking" -- aka having
 * no value defined for that tile.
 *
 * Currently, this serves as a glorified "null", but is made a separate class to support different
 * types of masking in the future.
 */
sealed class LPChartEntry {
    abstract fun to_lp(): LPBinaryExpr

    /** Coerces to a binary expression, treating masked values as One (true). */
    fun to_lp_masked_one(): LPBinaryExpr =
        when (this) {
            is Masked -> One
            is VarCell -> v
        }

    /** Coerces to a binary expression, treating masked values as Zero (false). */
    fun to_lp_masked_zero(): LPBinaryExpr =
        when (this) {
            is Masked -> Zero
            is VarCell -> v
        }
}

data object Masked : LPChartEntry() {
    override fun to_lp(): LPBinaryExpr = Zero
}

class VarCell(val v: LPBinaryExpr) : LPChartEntry() {
    override fun to_lp(): LPBinaryExpr = v
}
