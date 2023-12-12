package grid_model.predicate

// TODO this might painting ourselves into a corner vis a vis having "these two tiles are equal"
//  style predicates. forcing to expand that as OR(AND(...), AND(...)) is a bit awkward... but maybe
//  it's fine?

/**
 * Single Point Grid Predicate
 *
 * One of the most fundamental building blocks of a grid model, this predicate represents a
 * particular single-point condition being true at a given index.
 */
data class SPGP(val ndix: List<Int>, val cond: SinglePointCondition) : BaseGridPredicate {
    val ndim = ndix.size

    infix fun translated(shift: List<Int>): SPGP =
        SPGP(ndix.zip(shift).map { (a, b) -> a + b }, cond)
}
