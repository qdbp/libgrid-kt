package kulp.transforms.ril

import kulp.*
import kulp.expressions.IntAffExpr
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
 * - any value less than or equal to zero is false
 * - any value greater than zero is true
 *
 * The outputs are plain affine expression and are not guaranteed to be reified. These expressions
 * will need to be `put` rather than `bind`ed, and for this reason any variables that are created
 * internally are attached to new children of the passed node.
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
        // TODO more efficient implementation
        val pq = implies(p, q)
        val qp = implies(q, p)
        return and(pq, qp)
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
                val sum = xs.bind_each("xor_clip") { it.bool_clip() }.lp_sum()
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
                val clipped = xs.bind_each("and_clip") { it.int_clip(ub = 1) }
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
            else -> xs.bind_each("or_clip") { it.int_clip(lb = 0) }.lp_sum()
        }
    }

    context(NodeCtx)
    fun or(vararg xs: LPAffExpr<Int>): LPAffExpr<Int> = or(xs.toList())

    context(NodeCtx)
    fun implies(p: LPAffExpr<Int>, q: LPAffExpr<Int>): LPAffExpr<Int> {
        val not_p = "not_p" { not(p).int_clip(lb = 0) }
        // we don't need an upper bound on q
        val q_clip = "q_clip" { q.int_clip(lb = 0) }
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
            else -> 1 - k + xs.bind_each("min_sat") { it.bool_clip() }.lp_sum()
        }
    }

    context(NodeCtx)
    fun max_sat(k: Int, xs: List<LPAffExpr<Int>>): LPAffExpr<Int> {
        return when {
            k < 0 -> never
            k == 0 -> and(xs.map { not(it) })
            k == xs.size - 1 -> or(xs.map { not(it) })
            k >= xs.size -> always
            else -> 1 + k - xs.bind_each("max_sat") { it.bool_clip() }.lp_sum()
        }
    }

    fun not(p: LPAffExpr<Int>): LPAffExpr<Int> = 1 - p

    val always: LPAffExpr<Int> = IntAffExpr(1)

    val never: LPAffExpr<Int> = IntAffExpr(0)
}
