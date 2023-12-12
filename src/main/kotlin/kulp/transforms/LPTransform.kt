package kulp.transforms

import kulp.BindCtx
import kulp.LPAffExpr
import kulp.LPContext
import kulp.LPRenderable
import kulp.variables.LPVar

abstract class LPTransform<N : Number> protected constructor(protected val y: LPVar<N>) :
    LPVar<N> by y {

    companion object {
        context(BindCtx)
        @JvmStatic
        protected fun <N : Number> self_by_example(
            example: LPAffExpr<N>,
            lb: N? = null,
            ub: N? = null
        ): LPVar<N> = example.dom.newvar(lb, ub)
    }

    final override fun as_primitive(ctx: LPContext): LPRenderable = y
}
