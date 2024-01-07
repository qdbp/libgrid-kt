package kulp.expressions

import ivory.interval.ClosedInterval
import kulp.LPAffExpr
import kulp.LPBounded
import kulp.LPRenderable
import kulp.minus
import kulp.variables.LPBinary
import kulp.variables.LiftedLPVar

/**
 * Wrapper union type over LP integer expressions that take values of either zero or one.
 *
 * This is kind of ugly, but lets us statically enforce [0, 1] bounds in many places where otherwise
 * we'd need to dynamically check (aka hope and pray) that more general expressions are not being
 * passed in.
 */
sealed class LPBinaryExpr(private val x: LPAffExpr<Int>) : LPAffExpr<Int> by x, LPBounded<Int> {
    operator fun not(): LPAffExpr<Int> =
        when (this) {
            is Zero -> One
            is One -> Zero
            is PosBinary -> NegBinary(b)
            is NegBinary -> PosBinary(b)
        }

    override val bounds = ClosedInterval(0, 1)
}

object Zero : LPBinaryExpr(IntConstExpr(0)) {
    override val bounds = ClosedInterval(0)

    override fun toString(): String = "0"
}

object One : LPBinaryExpr(IntConstExpr(1)) {
    override val bounds = ClosedInterval(1)

    override fun toString(): String = "1"
}

class PosBinary(val b: LPBinary) : LPBinaryExpr(b), LPRenderable by b, LiftedLPVar<LPBinary> {
    override fun lower(): LPBinary = b

    override fun toString(): String = "+${b.path}"
}

class NegBinary(val b: LPBinary) : LPBinaryExpr(1 - b) {
    override fun toString(): String = "~${b.path}"
}
