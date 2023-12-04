package kulp

import kulp.variables.*
import model.SegName

/**
 * A variable is an affine expression with a particular form, that has a name and therefore a
 * representation in the output model.
 *
 * For this base interface, the representation might be complex (i.e. be attached to constraints and
 * involve auxiliaries).
 *
 * For the variable as commonly understood, see [PrimitiveLPVariable]
 */
interface LPVariable<N : Number> : LPAffExpr<N>, LPRenderable {

    val domain: LPDomain
    val lb: N?
    val ub: N?

    @Suppress("UNCHECKED_CAST")
    fun copy_as(name: SegName): LPVariable<N> =
        when (this) {
            is LPReal -> LPReal(name)
            is LPBinary -> LPBinary(name)
            is LPNonnegativeInteger -> LPNonnegativeInteger(name)
            is LPInteger -> LPInteger(name)
            else -> throw NotImplementedError("Cannot yet copy $this")
        }
            as LPVariable<N>
}
