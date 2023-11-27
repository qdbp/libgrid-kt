package kulp.variables

import kulp.*
import kulp.constraints.LPConstraint
import kulp.constraints.LP_LEQ
import model.SegName

abstract class LPVariable(override val name: SegName, val domain: LPDomain) :
    LPRenderable, LPExprLike {

    final override fun is_primitive(ctx: MipContext): Boolean = true

    abstract fun intrinsic_constraints(): List<LPConstraint>

    /** Convenience method for giving names to intrinsic constraints. */
    protected fun intrinsic_prefix(): SegName {
        return name.refine("${javaClass.simpleName}_intrinsic")
    }

    final override fun render(ctx: MipContext): List<LPRenderable> {
        val le_bigm = LP_LEQ(name.refine("le_bigm"), this, ctx.bigM - 1)
        val ge_neg_bigm = LP_LEQ(name.refine("ge_bigm"), -ctx.bigM + 1, this)
        return intrinsic_constraints() + listOf(le_bigm, ge_neg_bigm, this)
    }

    override fun as_expr(): LPAffineExpression = LPAffineExpression(mapOf(this.name to 1.0), 0.0)

    fun copy_as(name: SegName): LPVariable =
        when (this) {
            is LPReal -> LPReal(name)
            is LPBinary -> LPBinary(name)
            is LPNonnegativeInteger -> LPNonnegativeInteger(name)
            is LPInteger -> LPInteger(name)
            else -> throw Exception("Unknown variable type")
        }

    operator fun plus(other: LPExprLike): LPAffineExpression {
        return this.as_expr() + other.as_expr()
    }

    operator fun plus(other: Number): LPAffineExpression {
        return LPAffineExpression(mapOf(name to 1.0), other.toDouble())
    }

    operator fun minus(other: LPExprLike): LPAffineExpression {
        return this.as_expr() - other.as_expr()
    }

    operator fun minus(other: Number): LPAffineExpression {
        return this.as_expr() - other.toDouble()
    }

    operator fun unaryMinus(): LPAffineExpression {
        return LPAffineExpression(mapOf(name to -1.0), 0.0)
    }

    operator fun times(other: Number): LPAffineExpression {
        return this.as_expr() * other.toDouble()
    }

    override fun toString(): String {
        return "${javaClass.simpleName}[${name}]"
    }
}
