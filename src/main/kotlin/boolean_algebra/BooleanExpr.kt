@file:Suppress("unused")

package boolean_algebra

import boolean_algebra.BooleanExpr.Companion.pred
import ivory.functor.fmap
import kotlin.reflect.KClass

// bind = flatten . fmap, but we use the kotlin term flatMap
// this should be used for partial evaluation/assignment.
fun <T> BooleanExpr<T>.flatMap(assign: (T) -> BooleanExpr<T>?): BooleanExpr<T> =
    this.fmap { assign(it) ?: pred(it) }
        .fmap {
            // performs an unsafe lowering that expects fmap to be implemented correctly
            when (it) {
                is Pred -> it.atom
                else -> throw RuntimeException("fmap implementation is unsound")
            }
        }

fun <T> BooleanExpr<T>.partial_evaluate(assign: (T) -> Boolean?): BooleanExpr<T> =
    this.flatMap(assign.fmap { it?.lift() })

fun <T> BooleanExpr<T>.partial_evaluate(map: Map<T, Boolean>): BooleanExpr<T> =
    this.partial_evaluate { map[it] }

fun <T> BooleanExpr<T>.partial_evaluate(
    true_assign: Collection<T> = setOf(),
    false_assign: Collection<T> = setOf()
): BooleanExpr<T> =
    this.partial_evaluate {
        when (it) {
            in true_assign -> true
            in false_assign -> false
            else -> null
        }
    }

fun BooleanExpr<Boolean>.evaluate(): Boolean = this.evaluate { it }

fun <T> BooleanExpr<T>.evaluate(assignment: (T) -> Boolean): Boolean =
    this.fmap(assignment.fmap(Boolean::lift)) is True

fun <T> BooleanExpr<T>.evaluate(true_assign: Collection<T>): Boolean =
    this.fmap { it in true_assign } is True

/**
 * This class forms the basic hierarchy of "traditional" boolean algebraic relations.
 *
 * Any more advanced (sugared) relations need to know how to decompose themselves into one of these.
 *
 * The constructors of subclasses are made effectively private through a context object trick,
 * forcing construction through the companion object functions. These functions maintain the
 * invariant that the resulting expression is always reduced -- that is any provable simplifications
 * are performed immediately.
 */
sealed class BooleanExpr<out T>(protected val terms: List<BooleanExpr<T>>) {

    /** Maps over the leaves */
    fun <V> fmap(op: (T) -> V): BooleanExpr<V> {
        return when (this) {
            is Pred -> return pred(op(this.atom))
            else -> push_fmap(this, terms.map { it.fmap(op) })
        }
    }

    override fun hashCode(): Int =
        throw IllegalArgumentException("Nontrivial boolean algebras are unhashable.")

    override fun equals(other: Any?): Boolean {
        if (other !is BooleanExpr<*>) return false
        if (other is BooleanConstant) return this === other
        throw IllegalArgumentException(
            "Equality testing of boolean algebras is an optimization problem in itself, and is" +
                "therefore not implemented. As a rule, any logic that requires you to query " +
                "alg1 == alg2 is most likely misconceived. You might be looking for " +
                "`~~`"
        )
    }

    private fun get_string_lines(): List<String> {
        val out = mutableListOf(this.javaClass.simpleName)
        for (term in terms) {
            out += term.toString().split("\n").map { "\t$it" }
        }
        return out
    }

    override fun toString(): String = get_string_lines().joinToString("\n")

    /**
     * Tests if two boolean algebra trees are: a) structurally identical b) matched leaves are equal
     * according to T's equals
     *
     * This is a sufficient, but (extremely!) not necessary, condition for equality.
     *
     * e.g. logically And(x, y) == And(y, x), but will not pass this test unless x `~~` y.
     */
    @Suppress("unused")
    infix fun <V> `~~`(other: BooleanExpr<V>): Boolean {
        if (other is BooleanConstant) return this === other
        if (other is Pred<*>) return (this is Pred<*>) && this.atom == other.atom
        return (other::class == this::class) &&
            this.terms.zip(other.terms).all { (a, b) -> a `~~` b }
    }

    fun <V> other_caller(other: BooleanExpr<V>): Boolean {
        return this `~~` other
    }

    companion object Companion {
        // todo hackish, should be farmed out in a more principled way to a "functor-like" base
        // -- a decent amount of progress toward this was done by moving all logic into the
        // bas companion and leaving the inheritors "thin". this moves us toward the "type is
        // node attr" paradigm of a generalizable tree, with the companion functions serving as a
        // visitor.

        // runs after fmap to "re-create" the expression tree. This will re-run simplification
        // code in order to make sure that we maintain the invariant that we are always reduced.
        private fun <T> push_fmap(
            expr: BooleanExpr<*>,
            new_substructure: List<BooleanExpr<T>>
        ): BooleanExpr<T> {
            return when (expr) {
                is True -> True
                is False -> False
                // Pred should not show up here! It must be handled by the caller since it is the
                // leaf
                // object that carries the type T and can't appear in a *-projected context
                is Not<*> -> not(new_substructure[0])
                is And<*> -> and(new_substructure)
                is Or<*> -> or(new_substructure)
                is Xor<*> -> xor(new_substructure)
                is Implies<*> -> implies(new_substructure[0], new_substructure[1])
                is Eq<*> -> eq(new_substructure[0], new_substructure[1])
                is SatCount<*> -> sat_count(new_substructure, expr.min_sat, expr.max_sat)
                else -> throw RuntimeException("unreachable")
            }
        }

        // really, this is the only non-trivial case. Everything else passes through to this.
        // pred's simplify is the identity modulo the "lifting identity":
        // Pred<Boolean>(true) <=> True
        // Pred<Boolean>(false) <=> False
        fun <T> pred(x: T): BooleanExpr<T> {
            return when (x) {
                is Boolean -> (x as Boolean).lift()
                is BooleanExpr<*> -> {
                    // note: we do not flatten here!
                    when (@Suppress("UNCHECKED_CAST") val it = x as BooleanExpr<T>) {
                        is True -> True
                        is False -> False
                        else -> it
                    }
                }
                else -> Private.run { Pred(x) }
            }
        }

        fun <T> not(x: BooleanExpr<T>): BooleanExpr<T> {
            return when (x) {
                is True -> False
                is False -> True
                is Not -> x.terms[0]
                else -> Private.run { Not(x) }
            }
        }

        fun <T> and(xs: List<BooleanExpr<T>>): BooleanExpr<T> {
            val simplified_xs = mutableListOf<BooleanExpr<T>>()
            AVBE.denest(And::class, xs).forEach {
                if (it is False) return False
                if (it !is True) simplified_xs.add(it)
            }
            return when (simplified_xs.size) {
                0 -> True
                1 -> simplified_xs[0]
                else -> Private.run { And(simplified_xs) }
            }
        }

        fun <T> and(vararg xs: BooleanExpr<T>): BooleanExpr<T> = and(xs.toList())

        fun <T> or(xs: List<BooleanExpr<T>>): BooleanExpr<T> {
            val simplified_xs = mutableListOf<BooleanExpr<T>>()
            AVBE.denest(Or::class, xs).forEach {
                if (it is True) return True
                if (it !is False) simplified_xs.add(it)
            }
            // can't recurse here, since we'd get stuck in a loop
            return when (simplified_xs.size) {
                0 -> False
                1 -> simplified_xs[0]
                else -> Private.run { Or(simplified_xs) }
            }
        }

        fun <T> or(vararg xs: BooleanExpr<T>): BooleanExpr<T> = or(xs.toList())

        fun <T> xor(xs: List<BooleanExpr<T>>): BooleanExpr<T> {
            var negate = false
            val simplified_xs = mutableListOf<BooleanExpr<T>>()
            AVBE.denest(Xor::class, xs).forEach {
                when (it) {
                    is True -> negate = !negate
                    is False -> {}
                    else -> simplified_xs.add(it)
                }
            }
            return when {
                simplified_xs.size == 0 -> negate.lift()
                simplified_xs.size == 1 && negate -> not(simplified_xs[0])
                simplified_xs.size == 1 -> simplified_xs[0]
                negate -> Private.run { !Xor(simplified_xs) }
                else -> Private.run { Xor(simplified_xs) }
            }
        }

        fun <T> xor(vararg xs: BooleanExpr<T>): BooleanExpr<T> = xor(xs.toList())

        fun <T> implies(x: BooleanExpr<T>, y: BooleanExpr<T>): BooleanExpr<T> {
            return when {
                x is False || y is True -> True
                x is True -> y
                y is False -> not(x)
                else -> Private.run { Implies(x, y) }
            }
        }

        fun <T> implies(x: T, y: T): BooleanExpr<T> = implies(pred(x), pred(y))

        fun <T> eq(x: BooleanExpr<T>, y: BooleanExpr<T>): BooleanExpr<T> {
            return when {
                x is False && y is False -> True
                x is True && y is True -> True
                x is True -> y
                y is True -> x
                x is False -> not(y)
                y is False -> not(x)
                else -> Private.run { Eq(x, y) }
            }
        }

        private fun <T> sat_count_handle_trivial(
            min_sat: Int,
            max_sat: Int,
            xs: List<BooleanExpr<T>>
        ): BooleanExpr<T>? {
            // trivial cases
            return when {
                min_sat > xs.size -> False
                min_sat == 0 && max_sat >= xs.size -> True
                min_sat == xs.size -> and(xs)
                // simple cases -- these are cheaper as and/or than as sat_count
                min_sat == 1 && max_sat == xs.size -> or(xs)
                else -> null
            }
        }

        fun <T> sat_count(
            xs: List<BooleanExpr<T>>,
            min_sat: Int,
            max_sat: Int = xs.size
        ): BooleanExpr<T> {
            var min_rem_sat = maxOf(0, min_sat)
            var max_rem_sat = minOf(xs.size, max_sat)
            if (min_rem_sat > max_rem_sat) return False
            val new_xs = mutableListOf<BooleanExpr<T>>()
            for (x in xs) {
                when (x) {
                    is True -> {
                        if (min_rem_sat > 0) min_rem_sat--
                        max_rem_sat--
                    }
                    is False -> {}
                    else -> new_xs.add(x)
                }
                if (max_rem_sat < 0) return False
            }

            sat_count_handle_trivial(min_rem_sat, max_rem_sat, new_xs)?.let {
                return it
            }

            return Private.run { SatCount(new_xs, min_rem_sat, max_rem_sat) }
        }

        fun <T> sat_count(
            ts: Collection<T>,
            min_sat: Int = 0,
            max_sat: Int = ts.size
        ): BooleanExpr<T> = sat_count(ts.map { pred(it) }, min_sat, max_sat)
    }
    // sugar
    operator fun not(): BooleanExpr<T> = not(this)
}

// dummy object to prohibit instantiating the classes outside of this file, while keeping them
// public.
private object Private

sealed class BooleanConstant : BooleanExpr<Nothing>(listOf()) {
    override fun hashCode(): Int = System.identityHashCode(this)

    override fun equals(other: Any?): Boolean = this === other
}

data object True : BooleanConstant()

data object False : BooleanConstant()

fun Boolean.lift(): BooleanConstant = if (this) True else False

/** Lifted form of wrapped type */
context(Private)
class Pred<T>(val atom: T) : BooleanExpr<T>(listOf()) {

    override fun hashCode(): Int = Pair(Pred::class, atom).hashCode()

    override fun equals(other: Any?): Boolean = (other is Pred<*>) && atom == other.atom

    override fun toString(): String = ":$atom"
}

context(Private)
class Not<T>(x: BooleanExpr<T>) : BooleanExpr<T>(listOf(x)) {
    val x
        get() = terms[0]

    override fun toString(): String = "~${terms[0]}"
}

/**
 * Associative Variadic Boolean Expression.
 *
 * Examples: And, Or, Xor
 *
 * These obey the rule that when nested inside their own type, they can be flattened.
 *
 * e.g. And(a, And(b, c), And(d, And(e, f))) <=> And(a, b, c, d, e, f)
 */
sealed class AVBE<T>(xs: List<BooleanExpr<T>>) : BooleanExpr<T>(xs) {
    val xs
        get() = terms

    companion object {
        @JvmStatic
        fun <T, C : KClass<out AVBE<*>>> denest(
            cls: C,
            xs: List<BooleanExpr<T>>
        ): List<BooleanExpr<T>> {
            return xs.flatMap { x ->
                when (x::class) {
                    cls -> denest(cls, (x as AVBE<T>).terms)
                    else -> listOf(x)
                }
            }
        }
    }
}

context(Private)
class And<T>(xs: List<BooleanExpr<T>>) : AVBE<T>(xs)

context(Private)
class Or<T>(xs: List<BooleanExpr<T>>) : AVBE<T>(xs)

context(Private)
class Xor<T>(xs: List<BooleanExpr<T>>) : AVBE<T>(xs)

context(Private)
class Implies<T>(x: BooleanExpr<T>, y: BooleanExpr<T>) : BooleanExpr<T>(listOf(x, y)) {
    val p
        get() = terms[0]

    val q
        get() = terms[1]
}

context(Private)
class Eq<T>(x: BooleanExpr<T>, y: BooleanExpr<T>) : BooleanExpr<T>(listOf(x, y)) {
    val a
        get() = terms[0]

    val b
        get() = terms[1]
}

/**
 * An unholy, twisted thing. Punishing to SAT solvers. (RIL can handle it gracefully, though.)
 *
 * evaluates True iff (at least min_sat and at most max_sat of the input arguments xs evaluate True)
 */
context(Private)
class SatCount<T>(xs: List<BooleanExpr<T>>, val min_sat: Int, val max_sat: Int = xs.size) :
    BooleanExpr<T>(xs) {
    init {
        // we allow max_sat >= xs.size because there's no real reason to forbid it
        // however, the two cases below are more likely to indicate a bug, so we check them
        require(min_sat >= 0) { "min_sat=$min_sat must be >= 0" }
        require(max_sat >= min_sat) { "max_sat=$max_sat must be >= min_sat=$min_sat" }
    }

    val xs
        get() = terms
}
