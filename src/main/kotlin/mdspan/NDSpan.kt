package mdspan

/**
 * Base interface for NDSpan-like objects.
 *
 * Really one should have no reason to provide an implementation other than the default one, this
 * interface was mostly factored out to support delegation.
 */
interface NDSpan<out T> : List<T> {
    val data: List<T>
    val shape: List<Int>
    val strides: List<Int>

    // indexing stuff
    val indices: List<List<Int>>

    // public operators
    operator fun get(vararg indices: Int): T

    operator fun get(indices: List<Int>): T

    fun slice(vararg prefix: Int): NDSpan<T>

    fun slice(vararg slice: SliceLike): NDSpan<T>

    fun slice(slice: List<SliceLike>): NDSpan<T>

    // shape manipulation
    fun squeeze(): NDSpan<T>

    fun reshape(new_shape: List<Int>): NDSpan<T>

    fun reshape(vararg new_shape: Int): NDSpan<T>

    // mapping
    fun <U> map(transform: (T) -> U): NDSpan<U>

    fun <U> mapNdIndexed(transform: (List<Int>, T) -> U): NDSpan<U>

    /**
     * returns a new MDSpan with two axes contracted. The two dimensions must be of equal size.
     *
     * They are removed from the shape, and a new axis is added as the last dimension.
     */
    fun <U, V> contract(ax0: Int, other: NDSpan<U>, ax1: Int, op: (T, U) -> V): NDSpan<V>

    /** Returns a new MDSpan with the given axis removed by applying a function to each slice. */
    fun <V> apply(axis: Int, op: (List<T>) -> V): NDSpan<V> {
        return apply_indexed(axis) { _, it -> op(it) }
    }

    /**
     * Returns a new MDSpan with the given axis removed by applying a function to each slice.
     *
     * Function accepts both the index and the slice.
     */
    fun <V> apply_indexed(axis: Int, op: (Int, List<T>) -> V): NDSpan<V> {
        return apply_subspace_indexed(listOf(axis)) { ix, it -> op(ix[0], it.data) }
    }

    /**
     * Returns a new MDSpan with the given subspace collapsed by applying a function to each
     * subspan. Returns an NDspan with dimensions complementary to those that were removed.
     *
     * Example: shape (10, 20, 30, 40) -apply_subspace(1, 3)-> shape (10, 30)
     */
    fun <V> apply_subspace(subspace: List<Int>, op: (NDSpan<T>) -> V): NDSpan<V> {
        return apply_subspace_indexed(subspace) { _, it -> op(it) }
    }

    fun <V> apply_subspace(vararg subspace: Int, op: (NDSpan<T>) -> V): NDSpan<V> =
        apply_subspace(subspace.toList(), op)

    /**
     * Returns a new MDSpan with the given subspace collapsed by applying a function to each
     * subspan. Returns an NDspan with dimensions complementary to those that were removed.
     *
     * Function accepts both the index and the subspan.
     *
     * Example: shape (10, 20, 30, 40) -apply_subspace(1, 3)-> shape (10, 30)
     */
    fun <V> apply_subspace_indexed(subspace: List<Int>, op: (List<Int>, NDSpan<T>) -> V): NDSpan<V>

    /** returns a new MDSpan with the given axis removed by folding. */
    fun <V> fold(axis: Int, init: V, op: (V, T) -> V): NDSpan<V>

    /** Computes the tensor product of this and other by binary op. */
    fun <U, V> outer(other: NDSpan<U>, op: (T, U) -> V): NDSpan<V>

    /** Computes the Hadamard product of this and other by binary op. */
    fun <U, V> hadamard(other: NDSpan<U>, op: (T, U) -> V): NDSpan<V>

    /** Computes the cross-correlation (aka "convolution" in ML-speak) */
    fun <U, V, W> xcorr_full(
        kernel: NDSpan<U>,
        op_mul: (T, U) -> V,
        sum_init: W,
        op_sum: (W, V) -> W
    ): NDSpan<W>
}