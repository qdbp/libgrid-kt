package kulp.transforms

import kulp.LPRenderable
import kulp.LPTransform
import kulp.MipContext
import kulp.LPConstraint
import kulp.variables.PrimitiveLPVariable
import model.LPName

/**
 * A simple pseudo-"transform" that passes through the given variable as an output, but attaches a
 * collection of constraints. This allows us to encapsulate a "variable with some constraints" and
 * treat it as a variable, with the constraints carried around by the `LPTransform` machinery.
 */
class Constrained<N : Number>(x: PrimitiveLPVariable<N>, val constraints: List<LPConstraint>) :
    LPTransform<N>(x) {
    constructor(
        x: PrimitiveLPVariable<N>,
        vararg constraints: LPConstraint
    ) : this(x, constraints.toList())

    override fun LPName.render_auxiliaries(ctx: MipContext): List<LPRenderable> = constraints

    // TODO currently we do not check if these constraints have anything at all do to with the
    //  variable we're wrapping. should we?
    fun requiring(vararg constraints: LPConstraint): Constrained<N> {
        return Constrained(output, this.constraints + constraints)
    }

    fun requiring(constraints: List<LPConstraint>): Constrained<N> {
        return Constrained(output, this.constraints + constraints)
    }
}
