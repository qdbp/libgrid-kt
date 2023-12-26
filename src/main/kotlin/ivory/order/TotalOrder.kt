package ivory.order

import ivory.order.PartialOrder.Companion.pgeq
import ivory.order.PartialOrder.Companion.pleq

interface TotalOrder<in T> : PartialOrder<T> {
    override fun T.cmp(other: T): Rel

    companion object {
        // being totally ordered allows us to implement strict comparisons in one operation
        context(TotalOrder<T>)
        infix fun <T, A : T, B : T> A.plt(other: B): Boolean = !(this pgeq other)

        context(TotalOrder<T>)
        infix fun <T, A : T, B : T> A.pgt(other: B): Boolean = !(this pleq other)

        context(TotalOrder<T>)
        infix fun <T> T.kcmp(other: T): Int {
            return when {
                this pleq other ->
                    when {
                        other pleq this -> 0
                        else -> -1
                    }
                else -> 1
            }
        }

        context(TotalOrder<T>)
        infix fun <T, A : T, B : T> A.psort(other: B): Pair<T, T> {
            val cmp = this cmp other
            return when (cmp) {
                Rel.LEQ -> this to other
                Rel.GEQ -> other to this
            }
        }

        context(TotalOrder<T>)
        fun <T> Iterable<T>.psort(): List<T> {
            val out = this.toMutableList()
            out.sortWith { a, b -> a kcmp b }
            return out
        }
    }
}
