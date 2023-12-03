package kulp.variables

import kulp.LPAffExpr
import kulp.LPDomain
import kulp.LPRenderable
import kulp.MipContext
import kulp.constraints.LP_LEQ
import model.SegName

/**
 *  A variable in a linear program.
 *
 *  Note: we are assuming that adapters are able to handle variable constraints in a special way,
 *  and these constraints are *not* added to `render`.
 */
abstract class LPVariable<N : Number>(override val name: SegName, val domain: LPDomain) :
    LPRenderable, LPAffExpr<N> {

    final override fun is_primitive(ctx: MipContext): Boolean = true

    final override fun render(ctx: MipContext): List<LPRenderable> {
        val le_bigm = LP_LEQ(name.refine("le_bigm"), this, ctx.bigM - 1)
        val ge_neg_bigm = LP_LEQ(name.refine("ge_bigm"), -ctx.bigM + 1, this)
        return listOf(le_bigm, ge_neg_bigm, this)
    }

    @Suppress("UNCHECKED_CAST")
    fun copy_as(name: SegName): LPVariable<N> =
        when (this) {
            is LPReal -> LPReal(name)
            is LPBinary -> LPBinary(name)
            is LPNonnegativeInteger -> LPNonnegativeInteger(name)
            is LPInteger -> LPInteger(name)
            else -> throw Exception("Unknown variable type")
        }
            as LPVariable<N>

    override fun toString(): String {
        return "${javaClass.simpleName}[${name}]"
    }
}
