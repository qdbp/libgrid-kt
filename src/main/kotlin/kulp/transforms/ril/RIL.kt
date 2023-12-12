package kulp.transforms.ril

import kulp.LPAffExpr
import kulp.NodeCtx
import kulp.expressions.IntAffExpr
import kulp.expressions.bool_clip
import kulp.expressions.int_clip
import kulp.lp_sum
import kulp.minus
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
        val pq = branch { implies(p, q) }
        val qp = branch { implies(q, p) }
        return and(pq, qp)
    }

    context(NodeCtx)
    fun xor(xs: List<LPAffExpr<Int>>): LPAffExpr<Int> = xor(*xs.toTypedArray())

    context(NodeCtx)
    fun xor(vararg xs: LPAffExpr<Int>): LPAffExpr<Int> {
        return when (xs.size) {
            // empty xor is false; not we could also do false_pinned(node) to skip making
            // the intermediate node
            0 -> IntAffExpr(0)
            // we promise a variable, so we reify the expression
            1 -> xs[0]
            else -> {
                val clipped = xs.mapIndexed { i, x -> "clip_$i" { x.bool_clip() } }
                val sum = clipped.lp_sum()
                "xor" { IntEQZWitness(sum - 1) }
            }
        }
    }

    context(NodeCtx)
    fun and(xs: List<LPAffExpr<Int>>): LPAffExpr<Int> = and(*xs.toTypedArray())

    context(NodeCtx)
    fun and(vararg xs: LPAffExpr<Int>): LPAffExpr<Int> {
        return when (xs.size) {
            0 -> IntAffExpr(0)
            1 -> xs[0]
            else -> {
                val clipped = xs.mapIndexed { i, x -> "clip_$i" { x.int_clip(ub = 1) } }
                clipped.lp_sum() - clipped.size + 1
            }
        }
    }

    context(NodeCtx)
    fun or(xs: List<LPAffExpr<Int>>): LPAffExpr<Int> = or(*xs.toTypedArray())

    context(NodeCtx)
    fun or(vararg xs: LPAffExpr<Int>): LPAffExpr<Int> {
        return when (xs.size) {
            0 -> IntAffExpr(1)
            1 -> xs[0]
            else -> xs.mapIndexed { i, x -> "lb_$i" { x.int_clip(lb = 0) } }.lp_sum()
        }
    }

    context(NodeCtx)
    fun implies(p: LPAffExpr<Int>, q: LPAffExpr<Int>): LPAffExpr<Int> {
        val not_p = "not_p" { (-p).int_clip(lb = 0) }
        // we don't need an upper bound on q
        val q_clip = "q_clip" { q.int_clip(lb = 0) }
        return not_p + q_clip
    }

    fun not(p: LPAffExpr<Int>): LPAffExpr<Int> = 1 - p
}
