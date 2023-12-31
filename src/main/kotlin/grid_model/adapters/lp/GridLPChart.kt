package grid_model.adapters.lp

import grid_model.GridIndex

// TODO these are somewhat primitive stubs, need to work through a few case studies to see what's
//  needed and what's easiest to work with
/**
 * The Grid -> LP information clearinghouse.
 *
 * The Chart carries exactly the information needed to map the underlying grid layout to concrete
 * variables that can be set to true and false by an LP solver. It has no notion of the problem's
 * constraints or objective.
 *
 * A major responsibility of the chart is to encode boundary conditions for all planes and
 * dimensions. As a rule, the grid's raw satisfaction algebra does not respect the grid dimensions,
 * as there is no good way to impose boundary conditions at the Algebra level. This is done by the
 * Chart, which maps out-of-range references in the algebra to appropriate LP expressions.
 */
data class GridLPChart(
    val index: GridIndex,
    val lpec: LPEntityChart,
    val lptc: LPTileChart,
    // TODO potential chart
    // TODO flow chart
)
