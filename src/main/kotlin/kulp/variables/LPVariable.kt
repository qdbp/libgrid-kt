package kulp.variables

import kulp.*
import kulp.constraints.LP_LEQ
import kulp.constraints.LPConstraint

abstract class LPVariable(override val name: LPName, val domain: LPDomain) : LPRenderable, LPExprLike {

    final override fun is_primitive(ctx: MipContext): Boolean = true
    abstract fun intrinsic_constraints(): List<LPConstraint>

    /**
     * Convenience method for giving names to intrinsic constraints.
     */
    protected fun intrinsic_prefix(): LPName {
        return name.refine("${javaClass.simpleName}_intrinsic")
    }

    final override fun render(ctx: MipContext): List<LPRenderable> {
        val le_bigm = LP_LEQ(name.refine("le_bigm"), this, ctx.bigM - 1)
        val ge_neg_bigm = LP_LEQ(name.refine("ge_bigm"), -ctx.bigM + 1, this)
        return intrinsic_constraints() + listOf(le_bigm, ge_neg_bigm, this)
    }

    override fun as_expr(): LPAffineExpression = LPAffineExpression(
        mapOf(this to 1.0),
        0.0
    )

    fun copy_as(name: LPName): LPVariable = when (this) {
        is LPReal -> LPReal(name)
        is LPInteger -> LPInteger(name)
        is LPNonnegativeInteger -> LPNonnegativeInteger(name)
        is LPBinary -> LPBinary(name)
        else -> throw Exception("Unknown variable type")
    }

    operator fun plus(other: LPVariable): LPAffineExpression {
        return LPAffineExpression(
            mapOf(this to 1.0, other to 1.0),
            0.0
        )
    }

    operator fun plus(other: Number): LPAffineExpression {
        return LPAffineExpression(
            mapOf(this to 1.0),
            other.toDouble()
        )
    }

    operator fun minus(other: LPVariable): LPAffineExpression {
        return LPAffineExpression(
            mapOf(this to 1.0, other to -1.0),
            0.0
        )
    }

    operator fun minus(other: Number): LPAffineExpression {
        return LPAffineExpression(
            mapOf(this to 1.0),
            -other.toDouble()
        )
    }

    operator fun unaryMinus(): LPAffineExpression {
        return LPAffineExpression(
            mapOf(this to -1.0),
            0.0
        )
    }

    override fun toString(): String {
        return "${javaClass.simpleName}[${name}]"
    }
}

