package ivory.algebra

import ivory.num.B
import ivory.num.R
import ivory.num.Z

interface Group<T> : Monoid<T> {
    operator fun T.unaryMinus(): T

    companion object {
        context(Group<T>)
        operator fun <T> T.div(other: T): T = this op (-other)
    }
}

// empty, just a coat-hanger for laws for now
interface AbelianGroup<T> : Group<T>

// lifted algebras
object RAdd : AbelianGroup<R> {
    override val id: R = R(0.0)

    override infix fun R.op(other: R): R = R(r + other.r)

    override fun R.unaryMinus(): R = R(-r)
}

object ZAdd : AbelianGroup<Z> {
    override val id: Z = Z(0)

    override infix fun Z.op(other: Z): Z = Z(z + other.z)

    override fun Z.unaryMinus(): Z = Z(-z)
}

object BXor : AbelianGroup<B> {
    override val id: B = B(false)

    override infix fun B.op(other: B): B = B(this.b xor other.b)

    override fun B.unaryMinus(): B = this
}

// base algebras
object DoubleAdd : AbelianGroup<Double> {
    override val id: Double = 0.0

    override infix fun Double.op(other: Double): Double = this + other

    override fun Double.unaryMinus(): Double = -this
}

object IntAdd : AbelianGroup<Int> {
    override val id: Int = 0

    override infix fun Int.op(other: Int): Int = this + other

    override fun Int.unaryMinus(): Int = -this
}
