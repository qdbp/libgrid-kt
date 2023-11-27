package test_mdspan

import kotlin.test.Test
import mdspan.NDSpan

class TestNDSpan {

    /** Tests contraction and folding by doing a dot product. */
    @Test
    fun testContractAndFold() {
        val mat1 = NDSpan(listOf(1, 2, 3, 4, 5, 6), listOf(3, 2)) // 3x2 matrix
        val mat2 = NDSpan(listOf(1, 2, 3, 4, 5, 6), listOf(2, 3)) // 2x3 matrix

        // ij,jl->il
        val mat3 = mat1.contract(1, mat2, 0) { a, b -> a * b }.fold(2, 0) { a, b -> a + b }
        val mat4 = mat1.contract(1, mat2, 0) { a, b -> a * b }.fold(-1, 0) { a, b -> a + b }

        var expect = NDSpan(listOf(9, 12, 15, 19, 26, 33, 29, 40, 51), listOf(3, 3))
        assert(mat3 == expect)
        assert(mat4 == expect)

        // note this is not mat2 @ mat1 but rather the transpose
        // aka ji,il->il
        expect = NDSpan(listOf(22, 49, 28, 64), listOf(2, 2))
        val mat5 = mat1.contract(0, mat2, 1) { a, b -> a * b }.fold(2, 0) { a, b -> a + b }
        assert(mat5 == expect)
    }

    @Test
    fun testSelfContract() {
        val mat1 = NDSpan(listOf(5, -3, 2, 15, -9, 6, 10, -6, 4), listOf(3, 3))
        val mat2 = mat1.contract(0, mat1, 1) { a, b -> a * b }.fold(2, 0) { a, b -> a + b }
        val expect = NDSpan.full(0, listOf(3, 3))
        assert(mat2 == expect)
    }

    @Test
    fun testSqueeze() {
        val mat1 = NDSpan(listOf(1, 2, 3, 4, 5, 6), listOf(3, 2, 1)) // 3x2x1 matrix
        val mat2 = NDSpan(listOf(1, 2, 3, 4, 5, 6), listOf(3, 2)) // 3x2 matrix
        assert(mat1.squeeze() == mat2)
    }

    @Test
    fun testApply() {
        val mat1 = NDSpan(listOf(2, 7, 2, 1, 9, 8, 9, 3), listOf(2, 2, 2))
        // average middle axis to double
        val expect = NDSpan(listOf(2.0, 4.0, 9.0, 5.5), listOf(2, 2))
        val mat2 = mat1.apply(1) { it.average() }
        assert(mat2 == expect)
    }

    @Test
    fun testOuterProductAndFold() {
        val words = listOf("foo", "bar", "baz")
        val other_words = listOf("now", "later", "never")

        val mat1 = NDSpan(words)
        val mat2 = NDSpan(other_words)

        val outer = mat1.outer(mat2) { a, b -> "$a $b" }
        val expect =
            NDSpan(
                listOf(
                    "foo now",
                    "foo later",
                    "foo never",
                    "bar now",
                    "bar later",
                    "bar never",
                    "baz now",
                    "baz later",
                    "baz never",
                ),
                listOf(3, 3)
            )
        assert(outer == expect)

        val count_os_and_bs = outer.fold(0, 0) { acc, s -> acc + s.count { it == 'o' || it == 'b' } }
        val expect_fold = NDSpan(listOf(7, 4, 4), listOf(3))
        assert(count_os_and_bs == expect_fold)
    }

    @Test
    fun testConvolve() {
        val mat1 = NDSpan(1 until 10, listOf(3, 3))
        val mat2 = NDSpan.full(0.25, listOf(2, 2))

        val conv = mat1.xcorr_full(mat2, { a, b -> a * b }, 0.0, { a, b -> a + b })
        val expect = NDSpan(listOf(0.25, 0.75, 1.25, 0.75, 1.25, 3.0  , 4.0  , 2.25, 2.75, 6.0  , 7.0  ,
            3.75, 1.75, 3.75, 4.25, 2.25), listOf(4, 4))
        assert(conv == expect)

        val mat3 = NDSpan.full(1.0, listOf(1, 1))
        val conv2 = mat1.xcorr_full(mat3, { a, b -> a * b }, 0.0, { a, b -> a + b })
        assert(conv2 == mat1.map { it.toDouble() })
    }
}
