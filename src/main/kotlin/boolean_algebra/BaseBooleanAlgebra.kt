package boolean_algebra

import ivory.functor.fmap

// bind = flatten . fmap, but we use the kotlin term flatMap
// this should be used for partial evaluation/assignment.
fun <T> BooleanAlgebra<T>.flatMap(assign: (T) -> BooleanAlgebra<T>?): BooleanAlgebra<T> =
    this.fmap { assign(it) ?: Pred(it) }
        .fmap {
            // performs an unsafe lowering that expects fmap to be implemented correctly
            when (it) {
                is Pred -> it.x
                else -> throw RuntimeException("fmap implementation is unsound")
            }
        }
        .simplify()

fun BooleanAlgebra<Boolean>.evaluate(): Boolean = this.flatMap { it.lift() }.simplify() is True

fun <T> BooleanAlgebra<T>.evaluate(true_assign: Set<T>): Boolean =
    this.fmap { it in true_assign }.simplify() is True

/**
 * This class forms the basic hierarchy of "traditional" boolean algebraic relations.
 *
 * Any more advanced (sugared) relations need to know how to decompose themselves into one of these.
 */
sealed class BooleanAlgebra<out T>(private val substructure: List<BooleanAlgebra<T>>) {

    /** Simplifies this expression to one of a finite set of primitive forms define below. */
    abstract fun simplify(): BooleanAlgebra<T>

    // sugar
    operator fun not(): BooleanAlgebra<T> = Not(this)

    /** Maps over the leaves */
    abstract fun <V> fmap(op: (T) -> V): BooleanAlgebra<V>

    override fun hashCode(): Int = throw IllegalArgumentException("Nontrivial are unhashable.")

    override fun equals(other: Any?): Boolean {
        if (other !is BooleanAlgebra<*>) return false
        if (other is BooleanConstant) return this === other
        throw IllegalArgumentException(
            "Equality testing of boolean algebras is an optimization problem in itself, and is" +
                "therefore not implemented. As a rule, any logic that requires you to query " +
                "alg1 == alg2 is most likely misconceived. You might be looking for " +
                "`~~`"
        )
    }

    /**
     * Tests if two boolean algebra trees are: a) structurally identical b) matched leaves are equal
     * according to T's equals
     *
     * This is a sufficient, but (extremely!) not necessary, condition for equality.
     *
     * e.g. logically And(x, y) == And(y, x), but will not pass this test unless x `~~` y.
     */
    @Suppress("unused")
    infix fun <V> `~~`(other: BooleanAlgebra<V>): Boolean {
        if (other is BooleanConstant) return this === other
        if (other is Pred<*>) return (this is Pred<*>) && this.x == other.x
        return (other::class == this::class) &&
            this.substructure.zip(other.substructure).all { (a, b) -> a `~~` b }
    }

    fun <V> other_caller(other: BooleanAlgebra<V>): Boolean {
        return this `~~` other
    }

    fun evaluate(assignment: (T) -> Boolean): Boolean =
        this.fmap(assignment.fmap(Boolean::lift)).simplify() is True
}

sealed class BooleanConstant : BooleanAlgebra<Nothing>(listOf()) {
    final override fun <V> fmap(op: (Nothing) -> V) = this

    final override fun simplify(): BooleanAlgebra<Nothing> = this

    override fun hashCode(): Int = System.identityHashCode(this)

    override fun equals(other: Any?): Boolean = this === other
}

object True : BooleanConstant() {
    override fun toString(): String = "True"
}

object False : BooleanConstant() {
    override fun toString(): String = "False"
}

fun Boolean.lift(): BooleanConstant = if (this) True else False

fun <T> T.lift(): BooleanAlgebra<T> = Pred(this).simplify()

/** Lifted form of wrapped type */
data class Pred<T>(val x: T) : BooleanAlgebra<T>(listOf()) {
    // really, this is the only non-trivial case. Everything else passes through to this.
    override fun <V> fmap(op: (T) -> V): Pred<V> = Pred(op(x))

    // pred's simplify is the identity modulo the identification:
    // Pred<Boolean>(true) <=> True
    // Pred<Boolean>(false) <=> False
    override fun simplify(): BooleanAlgebra<T> {
        return when (this.x) {
            is Boolean -> (this.x as Boolean).lift()
            is BooleanAlgebra<*> -> {
                // note: we do not flatten here!
                val it = this.x as BooleanAlgebra<*>
                when (it.simplify()) {
                    is True -> True
                    is False -> False
                    else -> this
                }
            }
            else -> this
        }
    }
}

data class Not<out T>(val x: BooleanAlgebra<T>) : BooleanAlgebra<T>(listOf(x)) {
    override fun <V> fmap(op: (T) -> V): Not<V> = Not(x.fmap(op))

    constructor(x: T) : this(Pred(x))

    override fun simplify(): BooleanAlgebra<T> {
        return when (val it = x.simplify()) {
            is True -> False
            is False -> True
            is Not -> it.x.simplify()
            else -> !it
        }
    }
}

/**
 * Associative Variadic Boolean Algebra.
 *
 * Examples: And, Or, Xor
 *
 * These obey the rule that when nested inside their own type, they can be flattened.
 *
 * e.g. And(a, And(b, c), And(d, And(e, f))) <=> And(a, b, c, d, e, f)
 */
sealed class AVBA<T>(xs: List<BooleanAlgebra<T>>) : BooleanAlgebra<T>(xs) {

    val xs: List<BooleanAlgebra<T>> =
        xs.flatMap { x ->
            val x_simple = x.simplify()
            when (x_simple::class) {
                this::class -> (x_simple as AVBA<T>).xs
                else -> listOf(x_simple)
            }
        }

    override fun simplify(): BooleanAlgebra<T> = this

    override fun toString(): String = "${this::class.simpleName}(${xs.joinToString(",")})"
}

class And<T>(xs: List<BooleanAlgebra<T>>) : AVBA<T>(xs) {
    constructor(vararg xs: BooleanAlgebra<T>) : this(xs.toList())

    override fun <V> fmap(op: (T) -> V): BooleanAlgebra<V> = And(xs.map { it.fmap(op) })

    override fun simplify(): BooleanAlgebra<T> {
        return when (xs.size) {
            0 -> True
            1 -> xs[0].simplify()
            else -> {
                // lazy evaluation
                val simplified_xs = mutableListOf<BooleanAlgebra<T>>()
                xs.forEach {
                    val it_simple = it.simplify()
                    if (it_simple is False) {
                        return@simplify False
                    } else if (it_simple !is True) {
                        simplified_xs.add(it_simple)
                    }
                }
                // can't recurse here, since we'd get stuck in a loop
                when (simplified_xs.size) {
                    0 -> True
                    1 -> simplified_xs[0]
                    else -> And(simplified_xs)
                }
            }
        }
    }

    fun de_morganize(): BooleanAlgebra<T> = !Or(xs.map { !it }).simplify()
}

class Or<T>(xs: List<BooleanAlgebra<T>>) : AVBA<T>(xs) {
    constructor(vararg xs: BooleanAlgebra<T>) : this(xs.toList())

    override fun <V> fmap(op: (T) -> V): BooleanAlgebra<V> = Or(xs.map { it.fmap(op) })

    override fun simplify(): BooleanAlgebra<T> {
        return when (xs.size) {
            0 -> False
            1 -> xs[0].simplify()
            else -> {
                // lazy evaluation
                val simplified_xs = mutableListOf<BooleanAlgebra<T>>()
                xs.forEach {
                    val it_simple = it.simplify()
                    if (it_simple is True) {
                        return@simplify True
                    } else if (it_simple !is False) {
                        simplified_xs.add(it_simple)
                    }
                }
                // can't recurse here, since we'd get stuck in a loop
                when (simplified_xs.size) {
                    0 -> False
                    1 -> simplified_xs[0]
                    else -> Or(simplified_xs)
                }
            }
        }
    }

    fun de_morganize(): BooleanAlgebra<T> = !And(xs.map { !it }).simplify()
}

class Xor<T>(xs: List<BooleanAlgebra<T>>) : AVBA<T>(xs) {
    constructor(vararg xs: BooleanAlgebra<T>) : this(xs.toList())

    override fun <V> fmap(op: (T) -> V): BooleanAlgebra<V> = Xor(xs.map { it.fmap(op) })

    override fun simplify(): BooleanAlgebra<T> {
        return when (xs.size) {
            0 -> False
            1 -> xs[0].simplify()
            else -> {
                val xs_simple = xs.map { it.simplify() }
                if (xs_simple.all { it is BooleanConstant }) {
                    (xs_simple.count { it is True } % 2 == 1).lift()
                } else {
                    val invert = xs_simple.count { it is True } % 2 == 1
                    val xs_out = xs_simple.filter { it !is True && it !is False }
                    when (invert) {
                        true -> !Xor(xs_out)
                        false -> Xor(xs_out)
                    }
                }
            }
        }
    }
}

data class Implies<T>(val x: BooleanAlgebra<T>, val y: BooleanAlgebra<T>) :
    BooleanAlgebra<T>(listOf(x, y)) {
    constructor(x: T, y: T) : this(Pred(x), Pred(y))

    override fun <V> fmap(op: (T) -> V): BooleanAlgebra<V> = Implies(x.fmap(op), y.fmap(op))

    override fun simplify(): BooleanAlgebra<T> {
        val x_simple = x.simplify()
        val y_simple = y.simplify()
        return if (x_simple is False || y_simple is True) {
            True
        } else if (x_simple is True) {
            y_simple
        } else if (y_simple is False) {
            Not(x_simple).simplify()
        } else {
            Implies(x_simple, y_simple)
        }
    }
}

data class Eq<T>(val x: BooleanAlgebra<T>, val y: BooleanAlgebra<T>) :
    BooleanAlgebra<T>(listOf(x, y)) {

    override fun <V> fmap(op: (T) -> V): BooleanAlgebra<V> = Eq(x.fmap(op), y.fmap(op))

    override fun simplify(): BooleanAlgebra<T> {
        val x_simple = x.simplify()
        val y_simple = y.simplify()
        return if (x_simple is False && y_simple is False) {
            True
        } else if (x_simple is True && y_simple is True) {
            True
        } else if (x_simple is True) {
            y_simple
        } else if (y_simple is True) {
            x_simple
        } else if (x_simple is False) {
            Not(y_simple).simplify()
        } else if (y_simple is False) {
            Not(x_simple).simplify()
        } else {
            Eq(x_simple, y_simple)
        }
    }
}
