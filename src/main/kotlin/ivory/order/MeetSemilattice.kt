package ivory.order

interface MeetSemilattice<T> : Poset<T> {
    /** Greatest Lower Bound -- ∧ */
    infix fun T.meet(other: T): T
}
