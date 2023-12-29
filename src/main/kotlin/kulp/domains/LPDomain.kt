package kulp.domains

import ivory.algebra.Ring
import ivory.algebra.Ring.Companion.radd
import ivory.algebra.Ring.Companion.rmul
import ivory.interval.ClosedInterval
import ivory.order.TotalOrder
import ivory.order.TotalOrder.Companion.psort
import kulp.BindCtx
import kulp.LPAffExpr
import kulp.LPPath
import kulp.variables.LPVar
import kotlin.reflect.KClass

sealed class LPDomain<N : Number>(
    val klass: KClass<N>,
    val ring: Ring<N>,
    val order: TotalOrder<N>,
) {

    val zero: N = ring.zero

    val one: N = ring.one

    context(BindCtx)
    abstract fun newvar(lb: N? = null, ub: N? = null): LPVar<N>

    abstract fun newexpr(terms: Map<LPPath, N>, constant: N): LPAffExpr<N>

    context(BindCtx)
    fun newvar(ival: ClosedInterval<N>): LPVar<N> = newvar(ival.lb, ival.ub)

    /**
     * Things are never happy when there's a function called "coerce" in the offing...
     *
     * However, we keep all the quite static and unchanging implementation details of when this is
     * used deeply under control in [LPAffExpr].
     *
     * This function should never need to be called by implementing classes directly.
     */
    abstract fun coerce(expr: LPAffExpr<*>): LPAffExpr<N>

    abstract fun coerce_number(n: Number): N

    // TODO this is an abomination... we need to put a generic "Algebra" into `ivory`
    //  then we can unify with BooleanAlgebra even. `ivory` is the key here

    fun max(a: N, b: N): N = order.run { a psort b }.second

    fun min(a: N, b: N): N = order.run { a psort b }.first

    operator fun N.unaryMinus(): N = ring.add.run { -this@unaryMinus }

    operator fun N.plus(other: N): N = ring.run { this@N radd other }

    operator fun N.minus(other: N): N = ring.run { this@minus radd other.unaryMinus() }

    operator fun N.times(other: N): N = ring.run { this@times rmul other }
}
