package mdspan

import drop_at
import kulp.LPAffExpr
import kulp.lp_sum
import times

inline fun <reified N : Number> NDSpan<LPAffExpr<N>>.lp_sum(): LPAffExpr<N> = this.data.lp_sum()

inline fun <reified N : Number> NDSpan<LPAffExpr<N>>.lp_sum(axis: Int): NDSpan<LPAffExpr<N>> =
    this.apply(axis) { it.lp_sum() }

/**
 * numpy ndarray like shaped and strided view over a flat List of arbitrary objects.
 *
 * Unlike numpy, this class is definitely NOT designed for fast numerical operations. Indeed, it
 * considers performance as a tertiary concern, behind ease-of-correctness and flexibility.
 *
 * For this reason, it has the following properties:
 * 1. it is agnostic to the backing List type and memory layout. This class is written with small to
 *    medium collections of opaque objects in mind, and the underlying in-memory structure of those
 *    objects is considered irrelevant.
 * 2. True to the name "Span", this class never mutates the underlying list, making copies for all
 *    relevant operations.
 * 3. Basic numerical operations are more cumbersome than in numpy, because they must go through the
 *    full type-agnostic infrastructure. There is no special handling of e.g. folding or applying
 *    numerical functions relative to arbitrary operations.
 *
 * Inherits the Iterable interface from List.
 */
class NDSpanImpl<out T>
private constructor(
    override val data: List<T>,
    override val shape: List<Int>,
    override val strides: List<Int>
) : List<T> by data, NDSpan<T> {
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

        fun <T> full(value: T, shape: List<Int>): NDSpanImpl<T> =
            NDSpanImpl(List(shape.prod()) { value }, shape)

        fun <T> full_by(shape: List<Int>, init: (List<Int>) -> T): NDSpanImpl<T> =
            NDSpanImpl(ndindex(shape).map(init), shape)

        /**
         * Returns the n-dimensional index corresponding to the given flat index and shape.
         *
         * Assumes default C-style strides.
         */
        fun get_ndix(flat_ix: Int, shape: List<Int>): List<Int> {
            require(flat_ix in 0 until shape.prod())
            val out = mutableListOf<Int>()
            var ix = flat_ix
            for (s in shape.reversed()) {
                out.add(ix % s)
                ix /= s
            }
            return out.reversed()
        }

        fun subspace_complement(shape: List<Int>, axes: List<Int>): List<Int> =
            shape.indices.mapNotNull { if (it in axes) null else shape[it] }

        private fun subspace_complement_keepdims(shape: List<Int>, axes: List<Int>): List<Int> =
            shape.indices.map { if (it in axes) 1 else shape[it] }
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

    // validators
    private fun check_valid_axes(axes: List<Int>) {
        require(axes.all { it in shape.indices })
    }

    // indexing stuff
    override val indices: List<List<Int>> by lazy { ndindex(shape) }

    private fun get_offset(indices: List<Int>): Int {
        require(indices.size == shape.size)
        require(indices.all { it >= 0 })
        val out = indices.zip(strides).sumOf { it.first * it.second }
        require(out < data.size) {
            "index out of bounds: $indices" +
                if (strides == calculate_default_strides(shape)) "" else " (strides = $strides)"
        }
        return out
    }

    private fun make_ax_positive(ax: Int): Int {
        return if (ax < 0) ax + shape.size else ax
    }

    // public operators
    override operator fun get(vararg indices: Int): T = data[get_offset(indices.asList())]

    override operator fun get(indices: List<Int>): T = data[get_offset(indices)]

    override fun slice(vararg prefix: Int): NDSpanImpl<T> {
        require(prefix.size <= shape.size)
        val slice = prefix.map { IDX(it) } + List(shape.size - prefix.size) { ALL }
        return slice(*slice.toTypedArray())
    }

    override fun slice(vararg slice: SliceLike): NDSpanImpl<T> {
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

        return NDSpanImpl(new_data, new_shape)
    }

    override fun slice(slice: List<SliceLike>): NDSpanImpl<T> = slice(*slice.toTypedArray())

    // shape manipulation
    override fun squeeze(): NDSpanImpl<T> {
        val new_shape = shape.filter { it != 1 }
        return reshape(new_shape)
    }

    override fun reshape(new_shape: List<Int>): NDSpanImpl<T> {
        require(new_shape.prod() == shape.prod())
        return NDSpanImpl(data, new_shape.toList())
    }

    override fun reshape(vararg new_shape: Int): NDSpanImpl<T> {
        return reshape(new_shape.toList())
    }

    // mapping
    override fun <V> map(transform: (T) -> V): NDSpanImpl<V> = data.map(transform).reshape(shape)

    override fun <U> mapNdIndexed(transform: (List<Int>, T) -> U): NDSpan<U> {
        val new_data = mutableListOf<U>()
        for (ndix in ndindex(shape)) {
            new_data.add(transform(ndix, this[ndix]))
        }
        return NDSpanImpl(new_data, shape)
    }

    // subspace map
    override fun <V> apply_subspace_indexed(
        subspace: List<Int>,
        op: (List<Int>, NDSpan<T>) -> V
    ): NDSpan<V> {
        if (subspace.isEmpty()) check_valid_axes(subspace)

        // keep the dummy 1 dimensions so ndindex produces valid indices into self
        val new_shape_expanded = subspace_complement_keepdims(shape, subspace)
        val new_data = mutableListOf<V>()

        for (ndix in ndindex(new_shape_expanded)) {
            val slice = ndix.mapIndexed { dim, out_ix -> if (dim in subspace) ALL else IDX(out_ix) }
            new_data += op(ndix.filterIndexed { ix, _ -> ix !in subspace }, this.slice(slice))
        }
        return NDSpanImpl(new_data, subspace_complement(shape, subspace))
    }

    // contract, fold, apply
    /**
     * returns a new MDSpan with two axes contracted. The two dimensions must be of equal size.
     *
     * They are removed from the shape, and a new axis is added as the last dimension.
     */
    override fun <U, V> contract(ax0: Int, other: NDSpan<U>, ax1: Int, op: (T, U) -> V): NDSpan<V> {
        val pos_ax0 = make_ax_positive(ax0)
        val pos_ax1 = make_ax_positive(ax1)

        require(pos_ax0 in shape.indices)
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
        return NDSpanImpl(new_data, new_shape)
    }

    /** returns a new MDSpan with the given axis removed by folding. */
    override fun <V> fold(axis: Int, init: V, op: (V, T) -> V): NDSpanImpl<V> {
        val pos_axis = make_ax_positive(axis)
        require(pos_axis in shape.indices)
        val new_shape = shape.toMutableList()
        new_shape[pos_axis] = 1
        val new_data = mutableListOf<V>()
        for (ndix in ndindex(new_shape)) {
            val slice = ndix.mapIndexed { dim, ix -> if (dim == pos_axis) ALL else IDX(ix) }
            val folded = slice(*slice.toTypedArray()).fold(init, op)
            new_data.add(folded)
        }
        new_shape.removeAt(pos_axis)
        return NDSpanImpl(new_data, new_shape)
    }

    /** Computes the tensor product of this and other by binary op. */
    override fun <U, V> outer(other: NDSpan<U>, op: (T, U) -> V): NDSpan<V> {
        val new_shape = shape + other.shape
        val new_data = mutableListOf<V>()
        for ((ndix0, ndix1) in ndindex(shape) * ndindex(other.shape)) {
            new_data.add(op(this[ndix0], other[ndix1]))
        }
        return NDSpanImpl(new_data, new_shape)
    }

    /** Computes the Hadamard product of this and other by binary op. */
    override fun <U, V> hadamard(other: NDSpan<U>, op: (T, U) -> V): NDSpan<V> {
        require(shape == other.shape)
        val new_data = mutableListOf<V>()
        for (ndix in ndindex(shape)) {
            new_data.add(op(this[ndix], other[ndix]))
        }
        return NDSpanImpl(new_data, shape)
    }

    /** Computes the cross-correlation (aka "convolution" in ML-speak) */
    override fun <U, V, W> xcorr_full(
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
            kernel@ for (kx in ndindex(kernel.shape)) {
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
        return NDSpanImpl(new_data, new_shape)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is NDSpan<*>) return false
        if (other.shape != shape) return false
        if (other.strides != strides) return false
        return data == other.data
    }

    override fun hashCode(): Int {
        var result = data.hashCode()
        result = 31 * result + shape.hashCode()
        result = 31 * result + strides.hashCode()
        return result
    }
}

infix fun <T> Iterable<T>.reshape(shape: List<Int>): NDSpanImpl<T> = NDSpanImpl(this, shape)

fun <T> Iterable<T>.reshape(vararg shape: Int): NDSpanImpl<T> = this reshape shape.toList()
