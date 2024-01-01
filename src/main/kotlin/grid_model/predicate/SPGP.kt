package grid_model.predicate

import grid_model.dimension.Dim
import grid_model.dimension.Vec

// TODO this might painting ourselves into a corner vis a vis having "these two tiles are equal"
//  style predicates. forcing to expand that as OR(AND(...), AND(...)) is a bit awkward... but maybe
//  it's fine?

/**
 * Single Point Grid Predicate
 *
 * One of the most fundamental building blocks of a grid model, this predicate represents a
 * particular single-point condition being true at a given index.
 */
data class SPGP<D : Dim<D>>(val vec: Vec<D>, val cond: SinglePointCondition) : GridPredicate {
    infix fun translated(shift: Vec<D>): SPGP<D> = SPGP(vec + shift, cond)
}
