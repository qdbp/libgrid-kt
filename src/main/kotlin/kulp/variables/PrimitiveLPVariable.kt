package kulp.variables

import kulp.LPRenderable
import kulp.LPVariable
import kulp.MipContext
import kulp.constraints.LPConstraint
import kulp.transforms.Constrained

/**
 * Interface for primitive variables that have a simple representation in the output model
 * - a single variable name with no auxiliaries
 * - at most a simple upper bound and a simple lower bound constraint
 * - a domain constraint
 *
 *   AKA what you think of as a variable when you read mathematical
 */
sealed class PrimitiveLPVariable<N : Number> : LPVariable<N> {
    override fun is_primitive(ctx: MipContext): Boolean = true

    override fun render(ctx: MipContext): List<LPRenderable> {
        // TODO this might be bad for performance/unnecessary
        // val le_bigm = LP_LEQ(name.refine("le_bigm"), this, ctx.bigM - 1)
        // val ge_neg_bigm = LP_LEQ(name.refine("ge_bigm"), -ctx.bigM + 1, this)
        return listOf(this)
    }

    infix fun requiring(constraints: List<LPConstraint>): Constrained<N> {
        require(constraints.isNotEmpty())
        return Constrained(this, constraints)
    }

}
