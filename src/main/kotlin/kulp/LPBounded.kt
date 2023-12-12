package kulp

/** Interface for LP objects having intrinsic bounds. */
interface LPBounded<N : Number> {
    val lb: N?
    val ub: N?
}
