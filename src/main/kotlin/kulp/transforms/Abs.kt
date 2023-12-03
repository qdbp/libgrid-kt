package kulp.transforms

import kotlin.math.roundToInt
import kulp.LPRenderable
import kulp.MipContext
import kulp.constraints.LPConstraint
import kulp.constraints.LP_EQ
import kulp.constraints.LP_LEQ
import kulp.variables.LPBinary
import kulp.variables.LPVariable
import model.SegName

class Abs<N : Number> private constructor(val y: LPVariable<N>, val x: LPVariable<N>) :
    LPTransform<N>(y) {

    constructor(x: LPVariable<N>) : this(x.copy_as(x.name.refine("abs")), x)

    // we can build our auxiliaries statically
    private var auxiliaries: MutableList<LPVariable<N>> = mutableListOf()
    // override val name: SegName = x.name.refine(transform_identifier())

    private val x_p = x.copy_as(name.refine("p"))
    private val x_m = x.copy_as(name.refine("m"))

    // z == 1 <=> x <= 0
    private val z_x_is_neg = LPBinary(name.refine("is_negative"))

    init {
        auxiliaries.add(x_p)
        auxiliaries.add(x_m)
    }

    override fun is_primitive(ctx: MipContext): Boolean = false

    /** We build constraints dynamically, since we need bigM to be known. */
    override fun render(ctx: MipContext): List<LPRenderable> {
        val cxs: MutableList<LPConstraint> = mutableListOf()

        // split halves are positive
        cxs.add(LP_LEQ(name.refine("xp_geq_0"), 0, x_p))
        cxs.add(LP_LEQ(name.refine("xm_geq_0"), 0, x_m))

        // split halves actually represent the absolute value
        cxs.add(LP_EQ(name.refine("xp_minus_xm_is_x"), x_p - x_m, x))
        cxs.add(LP_EQ(name.refine("y_eq_xm_plus_xp"), y, x_m + x_p))

        // z == 1 <=> x <= 0
        val M = ctx.bigM.roundToInt()
        cxs.add(LP_LEQ(name.refine("xm_z_switch"), x_p, !z_x_is_neg * M))
        cxs.add(LP_LEQ(name.refine("xp_z_switch"), x_m, z_x_is_neg * M))

        return auxiliaries + cxs
    }
}
