package ivory.order

interface JoinSemilattice<T> : PartialOrder<T> {
    /** Least Upper Bound -- ∨ */
    infix fun T.join(other: T): T
}
