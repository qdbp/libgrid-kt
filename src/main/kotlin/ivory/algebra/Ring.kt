package ivory.algebra

import ivory.num.B
import ivory.num.R
import ivory.num.Z

// sadly, we can't extend Monoid and AbelianGroup here because `op` would be fundamentally
// ambiguous We could pick just one for convenience, but that would lead to confusion. Instead, we
// expose the underlying monoid and group as fields. This will lead to annoyance in places where we
// e.g. have a ring but want the additive group, since we'll need to do with(ring.add) { ... }
// explicitly, but this is better than ambiguity imho.
// TODO we use this as a commutative ring, we should distinguish the two even if with a passthrough
//  interface
interface Ring<T> {

    val add: AbelianGroup<T>

    val mul: Monoid<T>

    val zero
        get() = add.id

    val one
        get() = mul.id

    // these operations implicitly find the upper bound of the input types
    companion object {
        context(Ring<T>)
        operator fun <T, A : T, B : T> A.plus(other: B): T = add.run { this@plus op other }

        context(Ring<T>)
        operator fun <T, A : T, B : T> A.times(other: B): T = mul.run { this@times op other }

        // give non-operator names to help resolve ambiguity in some call sites
        context(Ring<T>)
        infix fun <T, A : T, B : T> A.rmul(other: B): T = this * other

        context(Ring<T>)
        infix fun <T, A : T, B : T> A.radd(other: B): T = this + other

        context(Ring<T>)
        operator fun <T, A : T> A.unaryMinus(): T = add.run { -this@unaryMinus }

        context(Ring<T>)
        operator fun <T, A : T, B : T> A.minus(b: B): T = this + (-b)
    }
}

// lifted rings
object RRing : Ring<R> {
    override val add: AbelianGroup<R> = RAdd
    override val mul: Monoid<R> = RMul
}

object ZRing : Ring<Z> {
    override val add: AbelianGroup<Z> = ZAdd
    override val mul: Monoid<Z> = ZMul
}

object XorRing : Ring<B> {
    override val add: AbelianGroup<B> = BXor
    override val mul: Monoid<B> = BAndM
}

// base rings
object DoubleRing : Ring<Double> {
    override val add: AbelianGroup<Double> = DoubleAdd
    override val mul: Monoid<Double> = DoubleMul
}

object IntRing : Ring<Int> {
    override val add: AbelianGroup<Int> = IntAdd
    override val mul: Monoid<Int> = IntMul
}

val <N : Number> N.natural_ring: Ring<N>
    get() {
        @Suppress("UNCHECKED_CAST")
        return when (this) {
            is Double -> DoubleRing
            is Int -> IntRing
            else -> throw NotImplementedError()
        }
            as Ring<N>
    }
