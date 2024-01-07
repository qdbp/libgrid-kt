package grid_model.predicate

import grid_model.Entity

/** This grid point has an entity of the given type located on it. */
data class HasAnyEntity(val entities: Collection<Entity<*>>) : PointPredicate {
    constructor(entity: Entity<*>) : this(setOf(entity))
}

data class HasEntityOrMasked(val entity: Entity<*>) : PointPredicate
