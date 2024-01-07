package grid_model.predicate

import grid_model.geom.Dim
import grid_model.geom.Vec

/**
 * Localized Point Predicate
 *
 * One of the most fundamental building blocks of a grid model, this predicate represents a
 * particular single-point condition being true at a given grid location. The interpretation of the
 * location is context-dependent -- it will be relative in all places except for the final boolean
 * expression assembly inside [grid_model.GridProblem]
 *
 * Individual [grid_model.Demand]s will be expressed as boolean expressions over these predicates.
 */
data class LPP<D : Dim<D>>(val vec: Vec<D>, val cond: PointPredicate) : GridPredicate<D> {
    override infix fun translated(shift: Vec<D>): LPP<D> = LPP(vec + shift, cond)
}
