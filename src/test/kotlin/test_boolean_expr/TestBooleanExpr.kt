package test_boolean_expr

import boolean_algebra.*
import boolean_algebra.BooleanExpr.Companion.and
import boolean_algebra.BooleanExpr.Companion.implies
import boolean_algebra.BooleanExpr.Companion.not
import boolean_algebra.BooleanExpr.Companion.or
import boolean_algebra.BooleanExpr.Companion.pred
import boolean_algebra.BooleanExpr.Companion.sat_count
import boolean_algebra.BooleanExpr.Companion.xor
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

private val TestAlgebra: BooleanExpr<String> =
    xor(
        and(listOf(pred("a"), pred("b"))),
        pred("c"),
        or(
            pred("d"),
            !(!pred("e")),
            !pred("f"),
        ),
        True,
        True,
        and(False),
        or(True),
        implies(pred("g"), True),
        implies(pred("h"), False),
    )

private val Simplified: BooleanExpr<String> =
    xor(
        and(listOf(pred("a"), pred("b"))),
        pred("c"),
        or(
            pred("d"),
            pred("e"),
            !pred("f"),
        ),
        !pred("h")
    )

class TestBooleanExpr {

    @Test
    fun test_simplify() {
        val simplified = TestAlgebra
        println(simplified)
        assert(simplified `~~` Simplified)
    }

    @Test
    fun test_evaluate_pred() {
        assert(pred("a").evaluate(setOf("a")))
        assert(!pred("a").evaluate(setOf("b")))
    }

    @Test
    fun test_evaluate_not() {
        assert(!not(pred("a")).evaluate(setOf("a")))
        assert(not(pred("a")).evaluate(setOf("b")))
    }

    @Test
    fun test_evaluate_and() {
        assert(and<Boolean>().evaluate())
        assert(and(listOf(pred("a"), pred("b"))).evaluate(setOf("a", "b")))
        assert(!and(listOf(pred("a"), pred("b"))).evaluate(setOf("a")))
        assert(!and(listOf(pred("a"), pred("b"))).evaluate(setOf("b")))
        assert(!and(listOf(pred("a"), pred("b"))).evaluate(setOf("c")))
    }

    @Test
    fun test_evaluate_or() {
        assert(!or<Boolean>().evaluate())
        assert(or(listOf(pred("a"), pred("b"))).evaluate(setOf("a", "b")))
        assert(or(listOf(pred("a"), pred("b"))).evaluate(setOf("a")))
        assert(or(listOf(pred("a"), pred("b"))).evaluate(setOf("b")))
        assert(!or(listOf(pred("a"), pred("b"))).evaluate(setOf("c")))
        assert(!or(listOf(pred("a"), pred("b"))).evaluate(setOf()))
    }

    @Test
    fun test_evaluate_xor() {
        assert(!xor<Boolean>().evaluate())
        assert(!xor(listOf(pred("a"), pred("b"))).evaluate(setOf("a", "b")))
        assert(xor(listOf(pred("a"), pred("b"))).evaluate(setOf("a")))
        assert(xor(listOf(pred("a"), pred("b"))).evaluate(setOf("b")))
        assert(!xor(listOf(pred("a"), pred("b"))).evaluate(setOf("c")))
    }

    @Test
    fun test_evaluate_implies() {
        assert(implies("a", "b").evaluate(setOf("a", "b")))
        assert(implies("a", "b").evaluate(setOf("b")))
        assert(implies("a", "b").evaluate(setOf()))
        assert(!implies("a", "b").evaluate(setOf("a")))
        assert(implies("a", "b").evaluate(setOf("a", "b", "c")))
        assert(!implies("a", "b").evaluate(setOf("a", "c")))
    }

    @Test
    fun test_evaluate_sat_count() {
        var sc = sat_count(listOf("a", "b", "c", "d", "e"), 2, 4)
        assertFalse(sc.evaluate(setOf()))
        assertFalse(sc.evaluate(setOf("a")))
        assertTrue(sc.evaluate(setOf("a", "b")))
        assertTrue(sc.evaluate(setOf("a", "b", "d")))
        assertTrue(sc.evaluate(setOf("a", "b", "d", "e")))
        assertFalse(sc.evaluate(setOf("a", "b", "d", "e", "c")))

        // unrestricted sat_count should default to allowing any number of variables, and thus
        // reducing instantly to true
        sc = sat_count(listOf("a", "b", "c"))
        assertIs<True>(sc)

        sc = sat_count(listOf("a", "b"), min_sat = 3)
        assertIs<False>(sc)

        sc = sat_count(listOf("a", "b", "c"), min_sat = 1)
        assertIs<True>(sc.partial_evaluate(listOf("a")))

        sc = sat_count(listOf("a", "b", "c"), max_sat = 1)
        assertIs<False>(sc.partial_evaluate(listOf("a", "b")))
    }
}
