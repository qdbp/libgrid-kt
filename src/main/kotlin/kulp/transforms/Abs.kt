package kulp.transforms

import kulp.LPAffineExpression
import kulp.LPRenderable
import kulp.MipContext
import kulp.constraints.LP_EQ
import kulp.constraints.LPLEQ
import kulp.constraints.LPConstraint
import kulp.variables.LPBinary
import kulp.variables.LPVariable

class Abs(private val x: LPVariable) : Transform() {

  // we can build our auxiliaries statically
  private var auxiliaries: MutableList<LPVariable> = mutableListOf()
  override val name: String
    get() = "${x.name}_Abs"

  private val x_p = x.copy_as("${x.name}_Abs_p")
  private val x_m = x.copy_as("${x.name}_Abs_m")

  // z == 1 <=> x <= 0
  private val z_x_is_negative = LPBinary("${x.name}_Abs_z_x_is_negative")

  init {
    auxiliaries.add(x_p)
    auxiliaries.add(x_m)
  }

  override fun is_primitive(ctx: MipContext): Boolean = false

  /** We build constraints dynamically, since we need bigM to be known. */
  override fun render(ctx: MipContext): List<LPRenderable> {
    val cxs: MutableList<LPConstraint> = mutableListOf()

    // split halves are positive
    cxs.add(LPLEQ("${x}_Abs_xp_geq_0", 0, x_p))
    cxs.add(LPLEQ("${x}_Abs_xm_geq_0", 0, x_m))

    // split halves actually represent the absolute value
    cxs.add(LP_EQ("${x}_Abs_xp_minus_xm_is_x", x_p - x_m, x))

    // bigM binds us
    cxs.add(LPLEQ("${x}_Abs_bigM_binds_xm", x_m, ctx.bigM - 1))
    cxs.add(LPLEQ("${x}_Abs_bigM_binds_xp", x_p, ctx.bigM - 1))

    // z == 1 <=> x <= 0
    cxs.add(LPLEQ("${x}_Abs_xm_z_switch", x_p, (-z_x_is_negative.as_expr() + 1) * ctx.bigM))
    cxs.add(LPLEQ("${x}_Abs_xp_z_switch", x_m, z_x_is_negative.as_expr() * ctx.bigM))

    return auxiliaries + cxs
  }

  override fun as_expr(): LPAffineExpression {
    return x_p + x_m
  }
}
