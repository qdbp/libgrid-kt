package kulp

import ivory.interval.ClosedInterval

/** Interface for LP objects having intrinsic bounds. */
interface LPBounded<N : Number>: LPBoundable<N> {

    override fun resolve_bounds(root: LPNode): ClosedInterval<N> = bounds

    val bounds: ClosedInterval<N>

    val lb: N?
        get() = bounds.lb

    val ub: N?
        get() = bounds.ub
}

/** Interface for LP objects having continent bounds requiring name resolution. */
interface LPBoundable<N : Number> {
    fun resolve_bounds(root: LPNode): ClosedInterval<N>
}
