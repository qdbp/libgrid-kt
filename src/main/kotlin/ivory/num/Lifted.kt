package ivory.num

import ivory.order.MeetSemilattice
import ivory.order.Rel
import kotlin.reflect.KClass
import kotlin.reflect.cast

/** The numerical tower, done right. */
sealed class Num(private val impl: Any) {
    override fun toString(): String = impl.toString()

    override fun equals(other: Any?): Boolean = impl == other

    override fun hashCode(): Int = impl.hashCode()

    fun unify(other: Num): Pair<Num, Num> {
        NumTypeLattice.run {
            val glb = this@Num::class meet other::class
            return glb.cast(this@Num) to glb.cast(other)
        }
    }
}

@Suppress("unused")
val KClass<Num>.impl_type: KClass<out Any>
    get() =
        when (this) {
            B::class -> Boolean::class
            Z::class -> Int::class
            R::class -> Double::class
            else -> error("unreachable")
        }

// no forced conversion methods, including no lower to "real", since it's not a given
// that reals will be the "lowest" type.
private object NumTypeLattice : MeetSemilattice<KClass<out Num>> {
    private val class_list: List<KClass<out Num>> = listOf(R::class, Z::class, B::class)

    private fun class_index(cls: KClass<out Num>): Int = class_list.indexOf(cls)

    override fun KClass<out Num>.meet(other: KClass<out Num>): KClass<out Num> {
        return class_list[minOf(class_index(this), class_index(other))]
    }

    override fun KClass<out Num>.cmp(other: KClass<out Num>): Rel {
        val i = class_index(this)
        val j = class_index(other)
        return when {
            i <= j -> Rel.LEQ
            else -> Rel.GEQ
        }
    }
}

// we do not implement any arithmetic operations on the numeric types themselves; these are
// injected by algebraic typeclasses. Each more derived type carries its "lowered" value in
// parallel. These could also be derived on the fly, but that would be more annoying. This tower
// is not designed for massive numerics!
// TODO really R is a lie here, our best is Q with bigints, but we degrade down to doubles anyway
//  in lp, so let's make believe.
open class R(val r: Double) : Num(r) {
    constructor(n: Number) : this(n.toDouble())

    constructor(b: Boolean) : this(if (b) 1.0 else 0.0)
}

open class Z(val z: Int) : R(z.toDouble()) {
    constructor(b: Boolean) : this(if (b) 1 else 0)
}

class B(val b: Boolean) : Z(if (b) 1 else 0)
