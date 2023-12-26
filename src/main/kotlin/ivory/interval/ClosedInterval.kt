package ivory.interval

import ivory.algebra.Monoid
import ivory.algebra.Ring
import ivory.algebra.Ring.Companion.plus
import ivory.algebra.Ring.Companion.times
import ivory.algebra.Ring.Companion.unaryMinus

class ClosedInterval<N : Number>(val lb: N? = null, val ub: N? = null) {

    constructor(point: N) : this(point, point)

    companion object {
        fun <N : Number> empty(): ClosedInterval<N> = ClosedInterval(null, null)

        context(Ring<N>)
        fun <N : Number> natural_monoid(): Monoid<ClosedInterval<N>> =
            object : Monoid<ClosedInterval<N>> {
                override val id: ClosedInterval<N> = ClosedInterval(zero)

                override fun ClosedInterval<N>.op(other: ClosedInterval<N>): ClosedInterval<N> =
                    this + other
            }
    }

    init {
        require(lb == null || ub == null || lb.toDouble() <= ub.toDouble()) {
            "Lower bound must be less than or equal to upper bound"
        }
    }

    context(Ring<N>)
    operator fun unaryMinus(): ClosedInterval<N> {
        return ClosedInterval(ub?.unaryMinus(), lb?.unaryMinus())
    }

    context(Ring<N>)
    operator fun <A : N> plus(other: A): ClosedInterval<N> = this + ClosedInterval(other)

    context(Ring<N>)
    operator fun <A : N> plus(other: ClosedInterval<A>): ClosedInterval<N> {
        val lb = this.lb?.let { a -> other.lb?.let { b -> a + b } }
        val ub = this.ub?.let { a -> other.ub?.let { b -> a + b } }
        return ClosedInterval(lb, ub)
    }

    context(Ring<N>)
    operator fun times(other: N): ClosedInterval<N> {
        val b0 = this.lb?.let { a -> a * other }
        val b1 = this.ub?.let { a -> a * other }
        return when {
            other.toDouble() >= 0.0 -> ClosedInterval(b0, b1)
            else -> ClosedInterval(b1, b0)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ClosedInterval<*>) return false
        return lb == other.lb && ub == other.ub
    }

    override fun hashCode(): Int {
        var result = lb?.hashCode() ?: 0
        result = 31 * result + (ub?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "[$lb, $ub]"
    }
}
