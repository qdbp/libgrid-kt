package kulp.expressions

import ivory.interval.ClosedInterval
import kulp.LPAffExpr
import kulp.LPBoundable
import kulp.LPNode
import kulp.NodeCtx
import kulp.variables.LPVar

abstract class LPSumExpr<N : Number> : LPAffExpr<N> {

    override fun resolve_bounds(root: LPNode): ClosedInterval<N> {
        dom.ring.run {
            var out: ClosedInterval<N> = ClosedInterval(constant)
            for ((path, coef) in terms) {
                val resolved =
                    root.find_var(path)
                        ?: throw IllegalStateException(
                            "Expr term path $path resolved to non-var ${root.find(path)}."
                        )
                out += (resolved as LPVar<N>).bounds * coef
            }
            return out
        }
    }

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
