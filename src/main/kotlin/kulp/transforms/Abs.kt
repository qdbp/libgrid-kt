package kulp.transforms

import kulp.LPAffineExpression
import kulp.LPName
import kulp.LPRenderable
import kulp.MipContext
import kulp.constraints.LP_EQ
import kulp.constraints.LP_LEQ
import kulp.constraints.LPConstraint
import kulp.variables.LPBinary
import kulp.variables.LPVariable

class Abs(private val x: LPVariable) : Transform() {

  // we can build our auxiliaries statically
  private var auxiliaries: MutableList<LPVariable> = mutableListOf()
  override val name: LPName = x.name.refine(transform_identifier())

  private val x_p = x.copy_as(name.refine("p"))
  private val x_m = x.copy_as(name.refine("m"))

  // z == 1 <=> x <= 0
  private val z_x_is_negative = LPBinary(name.refine("is_negative"))

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

    // z == 1 <=> x <= 0
    cxs.add(LP_LEQ(name.refine("xm_z_switch"), x_p, (-z_x_is_negative.as_expr() + 1) * ctx.bigM))
    cxs.add(LP_LEQ(name.refine("xp_z_switch"), x_m, z_x_is_negative.as_expr() * ctx.bigM))

    return auxiliaries + cxs
  }

  override fun as_expr(): LPAffineExpression {
    return x_p + x_m
  }
}
