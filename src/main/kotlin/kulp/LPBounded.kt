package kulp

import ivory.interval.ClosedInterval

/** Interface for LP objects having intrinsic bounds. */
interface LPBounded<N : Number> {

    val bounds: ClosedInterval<N>

    val lb: N?
        get() = bounds.lb

    val ub: N?
        get() = bounds.ub
}
