package ivory.order

interface MeetSemilattice<T> : PartialOrder<T> {
    /** Greatest Lower Bound -- ∧ */
    infix fun T.meet(other: T): T
}
