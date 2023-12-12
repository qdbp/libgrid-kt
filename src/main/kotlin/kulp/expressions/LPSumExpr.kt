package kulp.expressions

import kulp.LPAffExpr
import kulp.NodeCtx
import kulp.variables.LPVar

abstract class LPSumExpr<N : Number> : LPAffExpr<N> {
    final override fun toString(): String {
        if (this.terms.size <= 3) {
            val out = terms.entries.joinToString(" + ") { "${it.value} ${it.key}" } + " + $constant"
            return out.replace("+ -", "- ")
        } else {
            return "... + $constant"
        }
    }

    context(NodeCtx)
    final override fun reify(): LPVar<N> = "reif" {
        dom.newvar().requiring("pin") { it eq this@LPSumExpr }
    }
}
