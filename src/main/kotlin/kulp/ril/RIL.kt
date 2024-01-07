package kulp.ril

import kulp.*
import kulp.expressions.IntConstExpr
import kulp.expressions.bool_clip
import kulp.expressions.int_clip
import kulp.transforms.IntEQZWitness

/**
 * Relaxed Integer Logic.
 *
 * Constructs affine expressions that witness the truth of various logical statements.
 *
 * These operate in what shall be termed as the "Relaxed Integer Logic" domain with the following
 * rules:
 * - all values are integral
 * - any value <= 0 <=> false
 * - any value >= 1 <=> true
 *
 * Logical operations, then, are defined as usual respecting the above interpretation.
 *
 * The specific value of a relaxed integer logic expression, other than if it is <=0 or >= 1, is
 * undefined (e.g. if an output is actually ==5, >=3, etc.). It is not guaranteed to be
 * deterministic and can change at any time. Relying on it for any purpose is undefined behavior.
 *
 * A note to users: RIL logic functions never create "scoping" nodes or otherwise attempt to avoid
 * name collisions. It is entirely the responsibility of the caller to structure RIL invocations
 * within appropriate namespacing constructs.
 */
object RIL {

    /**
     * Important note: RIL equivalence is not integer equality. Precisely, it is: x >= 1 <=> y >= 1
     *
     * e.g. x = 10, y = 20 still witnesses true, as does x == 0 and y = -1000
     */
    context(NodeCtx)
    fun equiv(p: LPAffExpr<Int>, q: LPAffExpr<Int>): LPAffExpr<Int> {
        val p_clip = branch("p") { p.bool_clip() }
        val q_clip = branch("q") { q.bool_clip() }
        return "eq" { IntEQZWitness(p_clip, q_clip) }
    }

    context(NodeCtx)
    fun xor(xs: List<LPAffExpr<Int>>): LPAffExpr<Int> {
        return when (xs.size) {
            // empty xor is false; not we could also do false_pinned(node) to skip making
            // the intermediate node
            0 -> never
            // we promise a variable, so we reify the expression
            1 -> xs[0]
            else -> {
                val sum = xs.branch_each { it.bool_clip() }.lp_sum()
                "xor" { IntEQZWitness(sum - 1) }
            }
        }
    }

    context(NodeCtx)
    fun xor(vararg xs: LPAffExpr<Int>): LPAffExpr<Int> = xor(xs.toList())

    context(NodeCtx)
    fun and(xs: List<LPAffExpr<Int>>): LPAffExpr<Int> {
        return when (xs.size) {
            0 -> always
            1 -> xs[0]
            else -> {
                val clipped = xs.branch_each { it.int_clip(ub = 1) }
                clipped.lp_sum() - clipped.size + 1
            }
        }
    }

    context(NodeCtx)
    fun and(vararg xs: LPAffExpr<Int>): LPAffExpr<Int> = and(xs.toList())

    context(NodeCtx)
    fun or(xs: List<LPAffExpr<Int>>): LPAffExpr<Int> {
        return when (xs.size) {
            0 -> never
            1 -> xs[0]
            else -> xs.branch_each { it.int_clip(lb = 0) }.lp_sum()
        }
    }

    context(NodeCtx)
    fun or(vararg xs: LPAffExpr<Int>): LPAffExpr<Int> = or(xs.toList())

    context(NodeCtx)
    fun implies(p: LPAffExpr<Int>, q: LPAffExpr<Int>): LPAffExpr<Int> {
        val not_p = branch { not(p).int_clip(lb = 0) }
        // we don't need an upper bound on q
        val q_clip = branch { q.int_clip(lb = 0) }
        return not_p + q_clip
    }

    context(NodeCtx)
    fun min_sat(k: Int, xs: List<LPAffExpr<Int>>): LPAffExpr<Int> {
        return when {
            // these are cheaper (half clips vs. full clips), so prefer them when we can
            k <= 0 -> always
            k == 1 -> or(xs)
            k == xs.size -> and(xs)
            k > xs.size -> never
            else -> 1 - k + xs.branch_each { it.bool_clip() }.lp_sum()
        }
    }

    context(NodeCtx)
    fun max_sat(k: Int, xs: List<LPAffExpr<Int>>): LPAffExpr<Int> {
        return when {
            k < 0 -> never
            k == 0 -> and(xs.map { not(it) })
            k == xs.size - 1 -> or(xs.map { not(it) })
            k >= xs.size -> always
            else -> 1 + k - xs.branch_each { it.bool_clip() }.lp_sum()
        }
    }

    fun not(p: LPAffExpr<Int>): LPAffExpr<Int> = 1 - p

    val always: LPAffExpr<Int> = IntConstExpr(1)

    val never: LPAffExpr<Int> = IntConstExpr(0)
}
