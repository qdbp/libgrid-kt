package ivory.order

interface PartialOrder<in T> {

    /** We leverage nullable types here and make incomparable <-> null */
    infix fun T.cmp(other: T): Rel?

    companion object {
        context(PartialOrder<T>)
        infix fun <T, A : T, B : T> A.pleq(other: B): Boolean =
            this == other || this cmp other == Rel.LEQ

        context(PartialOrder<T>)
        infix fun <T, A : T, B : T> A.pgeq(other: B): Boolean =
            this == other || this cmp other == Rel.GEQ

        // we do not use ==, since we allow two objects to be logically equal with respect to a
        // given order independently of JVM equality
        context(PartialOrder<T>)
        infix fun <T, A : T, B : T> A.peq(other: B): Boolean = this pleq other && this pgeq other

        context(PartialOrder<T>)
        infix fun <T, A : T, B : T> A.plt(other: B): Boolean = this pleq other && !(other pleq this)

        context(PartialOrder<T>)
        infix fun <T, A : T, B : T> A.pgt(other: B): Boolean = this pgeq other && !(other pgeq this)
    }
}
