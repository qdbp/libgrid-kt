package kulp

import kulp.variables.LPVar

abstract class LPTransform<N : Number>(node: LPNode, domain: LPDomain<N>) :
    LPVar<N>(node, domain) {

    final override fun decompose(ctx: LPContext) {
        val out = node grow this.domain::newvar named "out"
        decompose_auxiliaries(out.node, out, ctx)
    }

    // shadowing is intentional to force implementers to type `this.node` if they really mean
    // to put an auxiliary onto the parent
    abstract fun decompose_auxiliaries(node: LPNode, out: LPVar<N>, ctx: LPContext)
}
