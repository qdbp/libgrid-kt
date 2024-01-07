package grid_model.predicate

import grid_model.geom.Dim
import grid_model.geom.Vec

/**
 * Base interface for "atomic" predicates that exist at the leaves of the grid problem's
 * satisfaction algebra.
 *
 * We invoke Dogma 2 here to demand that each predicate be able to be relocated by some translation
 * in a structure-preserving way.
 */
sealed interface GridPredicate<D : Dim<D>> {
    infix fun translated(shift: Vec<D>): GridPredicate<D>
}
