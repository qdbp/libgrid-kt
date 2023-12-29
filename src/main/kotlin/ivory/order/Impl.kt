package ivory.order

import ivory.num.R

// lifted orders
object RealOrder : TotalOrder<R> {
    override fun R.cmp(other: R): Rel =
        when {
            this.r <= other.r -> Rel.LEQ
            else -> Rel.GEQ
        }
}

// base orders
object DoubleOrder : TotalOrder<Double> {
    override fun Double.cmp(other: Double): Rel =
        when {
            this <= other -> Rel.LEQ
            else -> Rel.GEQ
        }
}

object IntOrder : TotalOrder<Int> {
    override fun Int.cmp(other: Int): Rel =
        when {
            this <= other -> Rel.LEQ
            else -> Rel.GEQ
        }
}

val <N : Number> N.natural_order: TotalOrder<N>
    get() {
        @Suppress("UNCHECKED_CAST")
        return when (this) {
            is Int -> IntOrder
            else ->
                object : TotalOrder<N> {
                    override fun N.cmp(other: N): Rel =
                        when {
                            this.toDouble() <= other.toDouble() -> Rel.LEQ
                            else -> Rel.GEQ
                        }
                }
        }
            as TotalOrder<N>
    }
