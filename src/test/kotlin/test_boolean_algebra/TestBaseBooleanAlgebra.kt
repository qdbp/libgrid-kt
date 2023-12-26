package test_boolean_algebra

import boolean_algebra.*
import kotlin.test.Test

val TestAlgebra: BooleanAlgebra<String> =
    Xor(
        And(listOf(Pred("a"), Pred("b"))),
        Pred("c"),
        Or(
            Pred("d"),
            !(!Pred("e")),
            !Pred("f"),
        ),
        True,
        True,
        And(False),
        Or(True),
        Implies(Pred("g"), True),
        Implies(Pred("h"), False),
    )

val Simplified: BooleanAlgebra<String> =
    Xor(
        And(listOf(Pred("a"), Pred("b"))),
        Pred("c"),
        Or(
            Pred("d"),
            Pred("e"),
            !Pred("f"),
        ),
        !Pred("h")
    )

class TestBooleanAlgebra {
    @Test
    fun test_simplify() {
        val simplified = TestAlgebra.simplify()
        println(simplified)
        assert(simplified `~~` Simplified)
    }

    @Test
    fun test_evaluate_pred() {
        assert(Pred("a").evaluate(setOf("a")))
        assert(!Pred("a").evaluate(setOf("b")))
    }

    @Test
    fun test_evaluate_not() {
        assert(!Not(Pred("a")).evaluate(setOf("a")))
        assert(Not(Pred("a")).evaluate(setOf("b")))
    }

    @Test
    fun test_evaluate_and() {
        assert(And<Boolean>().evaluate())
        assert(And(listOf(Pred("a"), Pred("b"))).evaluate(setOf("a", "b")))
        assert(!And(listOf(Pred("a"), Pred("b"))).evaluate(setOf("a")))
        assert(!And(listOf(Pred("a"), Pred("b"))).evaluate(setOf("b")))
        assert(!And(listOf(Pred("a"), Pred("b"))).evaluate(setOf("c")))
    }

    @Test
    fun test_evaluate_or() {
        assert(!Or<Boolean>().evaluate())
        assert(Or(listOf(Pred("a"), Pred("b"))).evaluate(setOf("a", "b")))
        assert(Or(listOf(Pred("a"), Pred("b"))).evaluate(setOf("a")))
        assert(Or(listOf(Pred("a"), Pred("b"))).evaluate(setOf("b")))
        assert(!Or(listOf(Pred("a"), Pred("b"))).evaluate(setOf("c")))
        assert(!Or(listOf(Pred("a"), Pred("b"))).evaluate(setOf()))
    }

    @Test
    fun test_evaluate_xor() {
        assert(!Xor<Boolean>().evaluate())
        assert(!Xor(listOf(Pred("a"), Pred("b"))).evaluate(setOf("a", "b")))
        assert(Xor(listOf(Pred("a"), Pred("b"))).evaluate(setOf("a")))
        assert(Xor(listOf(Pred("a"), Pred("b"))).evaluate(setOf("b")))
        assert(!Xor(listOf(Pred("a"), Pred("b"))).evaluate(setOf("c")))
    }

    @Test
    fun test_evaluate_implies() {
        assert(Implies("a", "b").evaluate(setOf("a", "b")))
        assert(Implies("a", "b").evaluate(setOf("b")))
        assert(Implies("a", "b").evaluate(setOf()))
        assert(!Implies("a", "b").evaluate(setOf("a")))
        assert(Implies("a", "b").evaluate(setOf("a", "b", "c")))
        assert(!Implies("a", "b").evaluate(setOf("a", "c")))
    }
}
