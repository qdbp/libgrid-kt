package ivory.num

import ivory.algebra.Ring.Companion.unaryMinus
import ivory.algebra.natural_ring
import ivory.order.TotalOrder.Companion.kcmp
import ivory.order.natural_order

sealed class Extended<N : Number> : Comparable<Extended<N>> {

    final override operator fun compareTo(other: Extended<N>): Int {
        return when (this) {
            is Finite -> {
                val num = this.num
                when (other) {
                    is Inf -> -1
                    is MInf -> 1
                    is Finite -> num.natural_order.run { num kcmp other.num }
                }
            }
            is MInf -> {
                when (other) {
                    is MInf -> 0
                    else -> -1
                }
            }
            is Inf -> {
                when (other) {
                    is Inf -> 0
                    else -> 1
                }
            }
        }
    }

    // note: we resist the temptation to write a fake Monoid here with the cheeky identification
    // Inf + MInf == 0 because this destroys associativity. (a + Inf) - Inf = 0, but a + (Inf - Inf)
    // = a?? I don't think so.
    operator fun unaryMinus(): Extended<N> {
        return when (this) {
            is Finite -> Finite(num.natural_ring.run { -num })
            is MInf -> Inf()
            is Inf -> MInf()
        }
    }
}

data class Finite<N : Number>(val num: N) : Extended<N>()

class Inf<N : Number> : Extended<N>()

class MInf<N : Number> : Extended<N>()
