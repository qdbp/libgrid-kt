package kulp.transforms

import kulp.LPAffExpr
import kulp.LPVariable
import kulp.bool_clip
import kulp.lp_sum
import kulp.variables.LPBinary
import model.LPName

/**
 * This object constructs variables that witness the truth of various logical statements.
 *
 * Specifically, it creates variables of int type that are greater than zero iff their respective
 * predicate evaluates to true when the input expression are interpreted logically as being true iff
 * they are greater than zero.
 */
object OpWitness {

    fun eq(name: LPName, lhs: LPAffExpr<Int>, rhs: LPAffExpr<Int>): LPVariable<Int> {
        return IntEQZWitness(name, lhs - rhs)
    }

    fun xor(name: LPName, vararg xs: LPAffExpr<Int>): LPVariable<Int> {
        return when (xs.size) {
            // empty xor is false
            0 -> LPBinary.mk_false_pinned(name)
            // we promise a variable, so we reify the expression
            1 -> xs[0].reify(name)
            else -> {
                val y = LPBinary(name)
                val clipped = with(name) { xs.mapIndexed { i, x -> x.bool_clip(+"clip_$i") } }
                val sum = clipped.lp_sum()
                return IntEQZWitness(name.refine("xor_bind"), y - sum)
            }
        }
    }

    fun and(name: LPName, vararg xs: LPAffExpr<Int>): LPVariable<Int> {
        return when (xs.size) {
            0 -> LPBinary.mk_false_pinned(name)
            1 -> xs[0].reify(name)
            else -> {
                val y = LPBinary(name)
                val clipped = with(name) { xs.mapIndexed { i, x -> x.bool_clip(+"clip_$i") } }
                val sum = clipped.lp_sum()
                return IntEQZWitness(name.refine("and_bind"), sum - clipped.size + 1 - y)
            }
        }
    }

    fun or(name: LPName, vararg xs: LPAffExpr<Int>): LPVariable<Int> {
        return when (xs.size) {
            0 -> LPBinary.mk_true_pinned(name)
            1 -> xs[0].reify(name)
            else -> {
                val y = LPBinary(name)
                val clipped = with(name) { xs.mapIndexed { i, x -> x.bool_clip(+"clip_$i") } }
                val sum = clipped.lp_sum()
                return IntEQZWitness(name.refine("or_bind"), y - sum)
            }
        }
    }
}
