package test_ivory

import ivory.algebra.IntRing
import ivory.interval.ClosedInterval
import kotlin.test.Test
import kotlin.test.assertEquals

class TestClosedInterval {

    @Test
    fun test_basic() {
        val ival = ClosedInterval(0, 10)
        assertEquals(ival.lb, 0)
        assertEquals(ival.ub, 10)
    }

    @Test
    fun test_add() {
        IntRing.run {
            val ival = ClosedInterval(0, 10)

            assertEquals(ival + 7, ClosedInterval(7, 17))
            assertEquals(ival + ival, ClosedInterval(0, 20))

            val ival2 = ClosedInterval(-10, -10)
            assertEquals(ival + ival2, ClosedInterval(-10, 0))

            val ival3 = ClosedInterval(null, 10)
            assertEquals(ival + ival3, ClosedInterval(null, 20))

            val ival4 = ClosedInterval.unbounded<Int>()
            assertEquals(ival + ival4, ClosedInterval.unbounded())
        }
    }

    @Test
    fun test_mul() {
        IntRing.run {
            val ival = ClosedInterval(0, 10)

            assertEquals(ClosedInterval(0, 70), ival * 7)
            assertEquals(ClosedInterval(-70, 0), ival * -7)
            assertEquals(ClosedInterval(0, 0), ival * 0)
            assertEquals(ClosedInterval(0, 10), ival * 1)

            val ival2 = ClosedInterval(null, -10)
            assertEquals(ClosedInterval(null, -20), ival2 * 2)
            assertEquals(ClosedInterval(20, null), ival2 * -2)

            val ival3 = ClosedInterval(-7, null)
            assertEquals(ClosedInterval(-21, null), ival3 * 3)
            assertEquals(ClosedInterval(null, 21), ival3 * -3)

            val empty = ClosedInterval<Int>(null, null)
            assertEquals(empty, empty * 1)
            assertEquals(empty, empty * 0)
        }
    }

    @Test
    fun test_check_in_bounds() {
        IntRing.run {
            val ival = ClosedInterval(0, 10)
            assertEquals(ClosedInterval.IvalRel.Contains, ival.check_in_bounds(0))
            assertEquals(ClosedInterval.IvalRel.Contains, ival.check_in_bounds(5))
            assertEquals(ClosedInterval.IvalRel.Contains, ival.check_in_bounds(10))
            assertEquals(ClosedInterval.IvalRel.GUB, ival.check_in_bounds(11))
            assertEquals(ClosedInterval.IvalRel.LLB, ival.check_in_bounds(-1))

            val ival2 = ClosedInterval(null, 10)
            assertEquals(ClosedInterval.IvalRel.Contains, ival2.check_in_bounds(0))
            assertEquals(ClosedInterval.IvalRel.Contains, ival2.check_in_bounds(10))
            assertEquals(ClosedInterval.IvalRel.GUB, ival2.check_in_bounds(11))

            val ival3 = ClosedInterval(0, null)
            assertEquals(ClosedInterval.IvalRel.Contains, ival3.check_in_bounds(0))
            assertEquals(ClosedInterval.IvalRel.Contains, ival3.check_in_bounds(5))
            assertEquals(ClosedInterval.IvalRel.LLB, ival3.check_in_bounds(-5))
        }
    }
}
