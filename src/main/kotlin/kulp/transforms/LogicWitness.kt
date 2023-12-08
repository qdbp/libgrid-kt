package kulp.transforms

import kulp.*
import kulp.variables.LPBinary
import kulp.variables.LPVar

/**
 * This object constructs variables that witness the truth of various logical statements.
 *
 * Specifically, it creates variables of int type that are greater than zero iff their respective
 * predicate evaluates to true when the input expression are interpreted logically as being true iff
 * they are greater than zero.
 */
object LogicWitness {

    fun eq(lhs: LPAffExpr<Int>, rhs: LPAffExpr<Int>): Free<out LPVar<Int>> {
        return { IntEQZWitness(it, lhs - rhs) }
    }

    fun xor(vararg xs: LPAffExpr<Int>): Free<out LPVar<Int>> {
        return { node ->
            when (xs.size) {
                // empty xor is false; not we could also do false_pinned(node) to skip making
                // the intermediate node
                0 -> LPBinary.false_pinned(node)
                // we promise a variable, so we reify the expression
                1 -> xs[0].reify(node)
                else -> {
                    val clipped = xs.mapIndexed { i, x -> node grow x.boolclipped named "clip_$i" }
                    val sum = clipped.lp_sum()
                    IntEQZWitness(node, sum - 1)
                }
            }
        }
    }

    fun and(vararg xs: LPAffExpr<Int>): Free<out LPVar<Int>> {
        return { node ->
            when (xs.size) {
                0 -> LPBinary.false_pinned(node)
                1 -> xs[0].reify(node)
                else -> {
                    val clipped = xs.mapIndexed { i, x -> node grow x.boolclipped named "clip_$i" }
                    val sum = clipped.lp_sum()
                    IntEQZWitness(node, sum - clipped.size + 1)
                }
            }
        }
    }

    fun or(vararg xs: LPAffExpr<Int>): Free<out LPVar<Int>> {
        return { node ->
            when (xs.size) {
                0 -> LPBinary.true_pinned(node)
                1 -> xs[0].reify(node)
                else -> {
                    xs.mapIndexed { i, x -> node grow x.int_clip(lb = 0) named "lb_$i" }
                        .lp_sum()
                        .reify(node)
                }
            }
        }
    }

    fun implies(p: LPAffExpr<Int>, q: LPAffExpr<Int>): Free<out LPVar<Int>> {
        return { node ->
            val not_p = 1 - (node grow p.boolclipped named "not_p")
            // we don't need an upper bound on q
            val q_clip = node grow q.int_clip(lb = 0) named "q_clip"
            (not_p + q_clip).reify(node)
        }
    }
}
