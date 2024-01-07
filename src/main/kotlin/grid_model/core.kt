package grid_model

import boolean_algebra.BEReducer
import boolean_algebra.BooleanExpr
import grid_model.predicate.GridPredicate
import grid_model.predicate.PointPredicate

/**
 * The main "lego block" of setting up satisfaction conditions for a grid problem: [BooleanExpr]s
 * over [GridPredicate]s, which in turn (as a rule) consist of localized [PointPredicate]s
 */
typealias BEGP<D> = BooleanExpr<GridPredicate<D>>

typealias BEPP = BooleanExpr<PointPredicate>

typealias GPReducer<D> = BEReducer<GridPredicate<D>>