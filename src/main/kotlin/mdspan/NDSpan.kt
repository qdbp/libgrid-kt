package mdspan

import drop_at
import times

/**
 * numpy ndarray like view over a flat List.
 *
 * Agnostic to the backing List type. This obviously trades performance for flexibility; this class
 * is *not* designed for large scale numerical computation. It's written with small to medium arrays
 * of opaque objects in mind.
 *
 * provides convenient methods for slicing and indexing.
 *
 * inherits the Iterable interface from List.
 */
open class NDSpan<out T>
private constructor(val data: List<T>, val shape: List<Int>, val strides: List<Int>) :

    // private calc methods
    List<T> by data {
    companion object {
        private fun calculate_default_strides(shape: List<Int>): List<Int> {
            val strides = mutableListOf<Int>()
            var stride = 1
            for (s in shape.reversed()) {
                strides.add(stride)
                stride *= s
            }
            return strides.reversed()
        }

        fun <T> full(value: T, shape: List<Int>): NDSpan<T> {
            return NDSpan(List(shape.prod()) { value }, shape)
        }

        fun <T> full_by(shape: List<Int>, init: (List<Int>) -> T): NDSpan<T> {
            val data = mutableListOf<T>()
            for (ndix in ndindex(shape)) {
                data.add(init(ndix))
            }
            return NDSpan(data, shape)
        }
    }

    private fun get_offset(vararg indices: Int): Int {
        require(indices.size == shape.size)
        require(indices.all { it >= 0 })
        return indices.zip(strides).sumOf { it.first * it.second }
    }

    private fun make_ax_positive(ax: Int): Int {
        return if (ax < 0) ax + shape.size else ax
    }

    // constructors and init
    constructor(
        data: Iterable<T>,
        shape: List<Int>
    ) : this(data.toList(), shape, calculate_default_strides(shape))

    constructor(data: List<T>) : this(data, listOf(data.size))

    init {
        require(strides.size == shape.size)
        require(data.size == shape.prod())
        require(shape.all { it > 0 })
    }

    // public operators
    operator fun get(vararg indices: Int): T {
        return data[get_offset(*indices)]
    }

    operator fun get(indices: List<Int>): T {
        return data[get_offset(*indices.toIntArray())]
    }

    fun slice(vararg prefix: Int): NDSpan<T> {
        require(prefix.size <= shape.size)
        val slice = prefix.map { IDX(it) } + List(shape.size - prefix.size) { ALL }
        return slice(*slice.toTypedArray())
    }

    fun slice(vararg slice: SliceLike): NDSpan<T> {
        val new_shape = mutableListOf<Int>()
        for ((i, s) in shape.withIndex()) {
            when (val index = slice[i]) {
                is ALL -> new_shape.add(s)
                is SLC -> {
                    require(index.start >= 0)
                    require(index.end <= s)
                    new_shape.add(index.end - index.start)
                }
                is IDX -> new_shape.add(1)
                is SEL -> {
                    require(index.indices.all { it in 0 until s })
                    new_shape.add(index.indices.size)
                }
            }
        }

        val new_data = mutableListOf<T>()

        for (new_ndix in ndindex(*new_shape.toIntArray())) {
            val shifted_index: List<Int> =
                new_ndix.zip(slice).map {
                    when (val index = it.second) {
                        is ALL -> it.first
                        is SLC -> it.first + index.start
                        is IDX -> index.index
                        is SEL -> index.indices[it.first]
                    }
                }
            new_data.add(this[shifted_index])
        }

        return NDSpan(new_data, new_shape)
    }

    fun slice(slice: List<SliceLike>): NDSpan<T> {
        return slice(*slice.toTypedArray())
    }

    // shape manipulation
    fun squeeze(): NDSpan<T> {
        val new_shape = shape.filter { it != 1 }
        return reshape(new_shape)
    }

    fun reshape(new_shape: List<Int>): NDSpan<T> {
        require(new_shape.prod() == shape.prod())
        return NDSpan(data, new_shape.toList())
    }

    fun reshape(vararg new_shape: Int): NDSpan<T> {
        return reshape(new_shape.toList())
    }

    fun <U> map(transform: (T) -> U): NDSpan<U> {
        return NDSpan(data.map(transform), shape)
    }

    // contract, fold, apply
    /**
     * returns a new MDSpan with two axes contracted. The two dimensions must be of equal size.
     *
     * They are removed from the shape, and a new axis is added as the last dimension.
     */
    fun <U, V> contract(ax0: Int, other: NDSpan<U>, ax1: Int, op: (T, U) -> V): NDSpan<V> {
        val pos_ax0 = make_ax_positive(ax0)
        val pos_ax1 = make_ax_positive(ax1)

        require(pos_ax0 in 0 until shape.size)
        require(pos_ax1 in 0 until other.shape.size)
        val new_shape =
            (shape drop_at pos_ax0) + (other.shape drop_at pos_ax1) + listOf(shape[pos_ax0])

        val new_data = mutableListOf<V>()
        for ((ndix0, ndix1) in
            ndindex(shape drop_at pos_ax0) * ndindex(other.shape drop_at pos_ax1)) {
            for (kx in 0 until shape[ax0]) {
                val ix0 = ndix0.toMutableList()
                ix0.add(pos_ax0, kx)
                val ix1 = ndix1.toMutableList()
                ix1.add(pos_ax1, kx)
                new_data.add(op(this[ix0], other[ix1]))
            }
        }
        return NDSpan(new_data, new_shape)
    }

    fun <V> apply(axis: Int, op: (List<T>) -> V): NDSpan<V> {
        val pos_axis = make_ax_positive(axis)
        require(pos_axis in 0 until shape.size)
        val new_shape = shape.toMutableList()
        new_shape[pos_axis] = 1
        val new_data = mutableListOf<V>()
        for (ndix in ndindex(new_shape)) {
            val slice = ndix.mapIndexed { dim, ix -> if (dim == pos_axis) ALL else IDX(ix) }
            val applied = op(slice(*slice.toTypedArray()))
            new_data.add(applied)
        }
        new_shape.removeAt(pos_axis)
        return NDSpan(new_data, new_shape)
    }

    /** returns a new MDSpan with the given axis removed by folding. */
    fun <V> fold(axis: Int, init: V, op: (V, T) -> V): NDSpan<V> {
        val pos_axis = make_ax_positive(axis)
        require(pos_axis in 0 until shape.size)
        val new_shape = shape.toMutableList()
        new_shape[pos_axis] = 1
        val new_data = mutableListOf<V>()
        for (ndix in ndindex(new_shape)) {
            val slice = ndix.mapIndexed { dim, ix -> if (dim == pos_axis) ALL else IDX(ix) }
            val folded = slice(*slice.toTypedArray()).fold(init, op)
            new_data.add(folded)
        }
        new_shape.removeAt(pos_axis)
        return NDSpan(new_data, new_shape)
    }

    /** Computes the tensor product of this and other by binary op. */
    fun <U, V> outer(other: NDSpan<U>, op: (T, U) -> V): NDSpan<V> {
        val new_shape = shape + other.shape
        val new_data = mutableListOf<V>()
        for ((ndix0, ndix1) in ndindex(shape) * ndindex(other.shape)) {
            new_data.add(op(this[ndix0], other[ndix1]))
        }
        return NDSpan(new_data, new_shape)
    }

    /** Computes the Hadamard product of this and other by binary op. */
    fun <U, V> hadamard(other: NDSpan<U>, op: (T, U) -> V): NDSpan<V> {
        require(shape == other.shape)
        val new_data = mutableListOf<V>()
        for (ndix in ndindex(shape)) {
            new_data.add(op(this[ndix], other[ndix]))
        }
        return NDSpan(new_data, shape)
    }

    /** Computes the cross-correlation (aka "convolution" in ML-speak) */
    fun <U, V, W> xcorr_full(
        kernel: NDSpan<U>,
        op_mul: (T, U) -> V,
        sum_init: W,
        op_sum: (W, V) -> W
    ): NDSpan<W> {
        require(shape.size == kernel.shape.size)
        val new_shape = shape.mapIndexed { i, s -> s + kernel.shape[i] - 1 }
        val new_data = mutableListOf<W>()
        // looping over all cells of the output...
        for (ix in ndindex(new_shape)) {
            var sum = sum_init
            // looping over all cells of the kernel...
            kernel@ for (kx in ndindex(kernel.shape))  {
                val input_ix = mutableListOf<Int>()
                // looping over dimensions to build the index into the input array.
                for (i in shape.indices) {
                    val out = ix[i] + kx[i] - kernel.shape[i] + 1
                    // if any dim is out of bounds, skip this kernel cell.
                    if (out < 0 || out >= shape[i]) {
                        continue@kernel
                    }
                    input_ix.add(out)
                }
                sum = op_sum(sum, op_mul(this[input_ix], kernel[kx]))
            }
            new_data.add(sum)
        }
        return NDSpan(new_data, new_shape)
    }

    // overrides of basic methods
    override fun equals(other: Any?): Boolean {
        if (other !is NDSpan<*>) return false
        return data == other.data && shape == other.shape
    }
}
