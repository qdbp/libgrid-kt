package kulp.variables

sealed interface ILPIntBound

sealed interface ILPRealBound

object LPInfinite : ILPIntBound, ILPRealBound {
    override fun toString(): String = "inf"
}

data class LPRealBound(val value: Double) : ILPRealBound {
    override fun toString(): String = if (value >= 0) {value.toString()} else {"m${-value}"}
}

data class LPIntBound(val value: Int) : ILPIntBound {
    override fun toString(): String = if (value >= 0) {value.toString()} else {"m${-value}"}
}

val Int.bound: ILPIntBound
    get() = LPIntBound(this)
val Double.bound: ILPRealBound
    get() = LPRealBound(this)
