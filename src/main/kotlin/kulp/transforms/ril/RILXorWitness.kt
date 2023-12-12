package kulp.transforms.ril

import kulp.*
import kulp.aggregates.LPOneOfN
import kulp.expressions.gt
import kulp.transforms.LPTransform
import kulp.variables.LPVar

/**
 * Relaxed Integer Logic implementation of Xor.
 *
 * This variable will >= 1 if and only if exactly one of the inputs is >= 1
 *
 * Requires xs to be non-empty.
 */
class RILXorWitness private constructor(private val xs: List<LPAffExpr<Int>>, self: LPVar<Int>) :
    LPTransform<Int>(self) {

    companion object {
        // workaround for KT-57183
        context(BindCtx)
        fun invoke(xs: List<LPAffExpr<Int>>): RILXorWitness {
            require(xs.isNotEmpty()) {
                "XorWitness requires at least one input. " +
                    "You should be using [RIL.xor()] as a nicer front-end that can " +
                    "handle all input sizes more efficiently. "
            }
            return RILXorWitness(xs, self_by_example(xs[0]))
        }
    }

    context(NodeCtx)
    override fun decompose(ctx: LPContext) {
        require(ctx is BigMCapability)
        val M = ctx.intM
        // this selector chooses the one element that >= 1
        val selector = "selector" { LPOneOfN(xs.size) }
        selector.arr.forEachIndexed { ix, it ->
            "le_bind_$ix" { it le (M * it) }
            "gt_bind_$ix" { it gt (-M * !it) }
        }
    }
}
