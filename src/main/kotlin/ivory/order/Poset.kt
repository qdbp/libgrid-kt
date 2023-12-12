package ivory.order

fun interface Poset<T> {

    enum class Rel {
        LEQ,
        GEQ,
    }

    /** We leverage nullable types here and make incomparable <-> null */
    infix fun T.pcmp(other: T): Rel?

    companion object {
        context(Poset<T>)
        infix fun <T> T.pleq(other: T): Boolean = this pcmp other == Rel.LEQ

        context(Poset<T>)
        infix fun <T> T.pgeq(other: T): Boolean = this pcmp other == Rel.GEQ
    }
}

// no point adding bounded lattice until we have abstract statics
