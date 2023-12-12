package ivory.order

interface JoinSemilattice<T> : Poset<T> {
    /** Least Upper Bound -- ∨ */
    infix fun T.join(other: T): T
}
