package ivory.algebra

// todo: we're jumping the gun for expedience here. will need to string out group, etc., later.
/** e.g. Reals, F2, etc. */
interface Field<T> : Ring<T> {

    fun T.recip(): T

    companion object {
        context(Field<T>)
        operator fun <T> T.div(other: T): T = mul.run { this@div op other.recip() }
    }
}
