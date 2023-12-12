package grid_model.predicate

/**
 * Base interface for objects representing some single-point on the underlying objects.
 *
 * Precisely because the point is unspecified, this is not a grid predicate proper, but can be
 * turned into one when provided with a concrete coordinate.
 */
sealed interface SinglePointCondition {
    infix fun at(coords: List<Int>): SPGP = SPGP(coords, this)
}
