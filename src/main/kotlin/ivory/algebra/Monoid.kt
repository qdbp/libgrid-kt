package ivory.algebra

import ivory.num.B
import ivory.num.R
import ivory.num.Z

interface Monoid<T> {
    val id: T

    infix fun T.op(other: T): T

    companion object {
        context(Monoid<T>)
        fun <T> Iterable<T>.mfold(): T = fold(id) { acc, t -> acc op t }
    }
}

// lifted mul monoids
object RMul : Monoid<R> {
    override val id: R = R(1.0)

    override infix fun R.op(other: R): R = R(this.r * other.r)
}

object ZMul : Monoid<Z> {
    override val id: Z = Z(1)

    override infix fun Z.op(other: Z): Z = Z(this.z * other.z)
}

object BAndM : Monoid<B> {
    override val id: B = B(true)

    override infix fun B.op(other: B): B = B(this.b && other.b)
}

object BOrM : Monoid<B> {
    override val id: B = B(false)

    override infix fun B.op(other: B): B = B(this.b || other.b)
}

object BXorM : Monoid<B> {
    override val id: B = B(false)

    override infix fun B.op(other: B): B = B(this.b xor other.b)
}

// base mul monoids
object DoubleMul : Monoid<Double> {
    override val id: Double = 1.0

    override infix fun Double.op(other: Double): Double = this * other
}

object IntMul : Monoid<Int> {
    override val id: Int = 1

    override infix fun Int.op(other: Int): Int = this * other
}
