package kulp.expressions

import ivory.interval.ClosedInterval
import kulp.LPAffExpr
import kulp.LPNode
import kulp.LPPath
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
        // setting the bound here is not necessary for the solver, since this variable is pinned.
        // it will, however, propagate static bounds knowledge to other kulp code.
        dom.newvar(resolve_bounds(root)).requiring("pin") { it eq this@LPSumExpr }
    }

    final override fun unaryMinus(): LPAffExpr<N> =
        dom.run { newexpr(terms.mapValues { -it.value }, -constant) }

    final override fun times(other: N): LPAffExpr<N> =
        dom.run { newexpr(terms.mapValues { it.value * other }, constant * other) }

    final override fun minus(other: LPAffExpr<N>): LPAffExpr<N> = this + (-other)

    final override fun minus(other: N): LPAffExpr<N> = this + ring.add.run { -other }

    final override fun plus(other: LPAffExpr<N>): LPAffExpr<N> {
        return dom.run {
            val new_terms = mutableMapOf<LPPath, N>()
            for ((k, v) in terms) {
                new_terms[k] = v
            }
            for ((k, v) in other.terms) {
                new_terms[k] = (new_terms[k] ?: zero) + v
            }
            dom.newexpr(new_terms, constant + other.constant)
        }
    }

    final override fun plus(other: N): LPAffExpr<N> =
        dom.run {
            return newexpr(terms, constant + other)
        }
}


