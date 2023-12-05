package kulp.variables

import kulp.LPConstraint
import kulp.LPVariable
import kulp.transforms.Constrained

/**
 * Interface for primitive variables that have a simple representation in the output model
 * - a single variable name with no auxiliaries
 * - at most a simple upper bound and a simple lower bound constraint
 * - a domain constraint
 *
 *   AKA what you think of as a variable when you read mathematical
 */
sealed class PrimitiveLPVariable<N : Number> : LPVariable<N> {

    infix fun requiring(constraints: List<LPConstraint>): Constrained<N> {
        require(constraints.isNotEmpty())
        return Constrained(this, constraints)
    }

}
