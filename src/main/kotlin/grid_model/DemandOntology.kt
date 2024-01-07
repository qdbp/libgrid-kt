package grid_model

import boolean_algebra.BooleanExpr
import boolean_algebra.BooleanExpr.Companion.equiv
import boolean_algebra.BooleanExpr.Companion.implies

/**
 * This class enumerates the logical modes in which an entity's can relate to the satisfaction of
 * its tile predicates.
 */
sealed interface DemandOntology {
    fun <T> relator(entity_exists: BooleanExpr<T>, extent_satisfied: BooleanExpr<T>): BooleanExpr<T>
}

/**
 * An ident entity obeys the following rule:
 *
 * extent satisfied <==> entity exists
 *
 * The existence or non-existence of the entity is completely logically equivalent to the
 * satisfaction of their extent predicates.
 */
data object IfAndOnlyIf : DemandOntology {
    override fun <T> relator(
        entity_exists: BooleanExpr<T>,
        extent_satisfied: BooleanExpr<T>
    ): BooleanExpr<T> = equiv(entity_exists, extent_satisfied)
}

// TODO the two below are dangerous because they are very easy to get wrong. might not even need
//  them.

/**
 * Advanced, and hard to use.
 *
 * Obeys the following rule:
 *
 * extent satisfied <== entity exists.
 *
 * Specifically, these entities WILL NOT exist unless their extent is satisfied. However, they MAY
 * BE OMITTED at the choosing of the solver even if the extent is satisfied.
 */
data object EntImpliesExt : DemandOntology {
    override fun <T> relator(
        entity_exists: BooleanExpr<T>,
        extent_satisfied: BooleanExpr<T>
    ): BooleanExpr<T> = implies(entity_exists, extent_satisfied)
}

/**
 * Advanced, and hard to use.
 *
 * Obeys the following rule:
 *
 * extent satisfied ==> entity exists.
 *
 * Specifically, these entities WILL exist when the extent it satisfied. However, additional
 * entities MAY be created at the choosing of the solver even if the extent is not satisfied.
 */
data object ExtImpliesEnt : DemandOntology {
    override fun <T> relator(
        entity_exists: BooleanExpr<T>,
        extent_satisfied: BooleanExpr<T>
    ): BooleanExpr<T> = implies(extent_satisfied, entity_exists)
}
