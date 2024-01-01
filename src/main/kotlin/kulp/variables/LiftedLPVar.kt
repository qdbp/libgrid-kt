package kulp.variables

/**
 * Shared interface for "lifted types" encapsulating [LPVar] variables.
 *
 * These usually take the form of wrapping sealed unions whose construction proves some property
 * about the wrapped variable. For technical and/or kotlin compiler bug reasons we can't always
 * delegate to LPVar directly from such wrappers -- this is where this interface comes in. It allows
 * us to have a uniform way to check if we can "lower" to an LPVar without needing to know about all
 * possible wrappers.
 *
 * The canonical example of this kind of wrapper [kulp.expressions.LPBinaryExpr]
 */
fun interface LiftedLPVar<V : LPVar<*>> {
    fun lower(): V
}
