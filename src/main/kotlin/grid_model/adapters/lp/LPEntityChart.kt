package grid_model.adapters.lp

import grid_model.Entity
import kulp.expressions.LPBinaryExpr

fun interface LPEntityChart {
    /** The bounded expression should be binary-like: 0 if entity is absent, 1 if present. */
    operator fun get(ix: List<Int>, entity: Entity<*>): LPBinaryExpr
}
