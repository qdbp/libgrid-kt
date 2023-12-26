package grid_model.predicate

import grid_model.Entity

/** This grid point has the given entity's formal center as it. */
data class IsEntity(val entity: Entity<*>) : SinglePointCondition
