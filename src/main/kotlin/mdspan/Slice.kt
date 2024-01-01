package mdspan

import requiring

sealed interface Slice

data object ALL : Slice

class IDX(val index: Int) : Slice

class SLC(val start: Int, val end: Int) : Slice

/**
 * The semantic interpretation of indices includes multiplicity and order.
 *
 * If, e.g., it is equal to [2, 1, 1, 0], when used as a selector the output will contain four
 * elements, [arr[2], arr[1], arr[1], arr[0]]
 */
class SEL(val indices: List<Int>) : Slice

sealed interface NDSlice {
    fun contains(ndix: List<Int>): Boolean
}

/**
 * This is the slice as commonly understood -- the points it selects is the cartesian product of the
 * points selected by each individual dimension
 *
 * Roughly equivalent to numpy's Basic Indexing.
 */
open class ProdIX(open val slice: List<Slice>) : NDSlice, List<Slice> by slice {
    constructor(vararg slice: Slice) : this(slice.toList())

    override fun contains(ndix: List<Int>): Boolean {
        return this.zip(ndix requiring { it.size == size }).all { (dim_slice, dim_ix) ->
            when (dim_slice) {
                is ALL -> true
                is IDX -> dim_slice.index == dim_ix
                is SLC -> dim_ix in dim_slice.start ..< dim_slice.end
                is SEL -> dim_ix in dim_slice.indices
            }
        }
    }
}

class NDPoint(override val slice: List<IDX>) : ProdIX(slice)

/**
 * A structured selection of points from the queried NDSpan into another one of arbitrary shape.
 *
 * The output shape is the shape of the selector.
 *
 * Roughly equivalent to numpy's Advanced Indexing.
 */
class SumIX(val selector: NDSpan<NDPoint>) : NDSlice {
    companion object {
        fun of_points(points: List<List<Int>>): SumIX =
            SumIX(NDSpanImpl(points.map { NDPoint(it.map(::IDX)) }))
    }

    // todo this is a slow one, leading to n^2 if used in naive intersect... need to handle this
    //  better if this ends up seeing more use.
    override fun contains(ndix: List<Int>): Boolean {
        return selector.any {
            it.slice.zip(ndix).all { (sel_ix, dim_ix) -> sel_ix.index == dim_ix }
        }
    }
}
