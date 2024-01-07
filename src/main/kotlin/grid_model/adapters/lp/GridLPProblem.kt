package grid_model.adapters.lp

import grid_model.Entity
import grid_model.GridProblem
import grid_model.GridSolution
import grid_model.tiles.Tile
import grid_model.plane.Plane
import kulp.LPProblem
import kulp.LPSolution

/**
 * Partial specialization of LPProblem to the grid setting, adding some functionality for passing
 * and extracting grid-level information.
 */
abstract class GridLPProblem : LPProblem() {

    abstract val parent: GridProblem<*>
    abstract val chart: GridLPChart

    // todo might want to also add the exiting problem as a parameter/context
    //  to be able to extract richer information. GridProblem and GridSolution should be
    //  highly cooperative classes since one never exists in isolation from the other.
    fun parse_solution(sol: LPSolution): GridSolution =
        object : GridSolution {
            override val problem: GridProblem<*> = parent

            override fun get_entities(ix: List<Int>): Collection<Entity<*>> =
                chart.index.all_entities().filter {
                    sol.value_of(chart.lpec[ix, it].to_lp()) == 1.0
                }

            override fun get_tiles(ix: List<Int>, plane: Plane): Collection<Tile> =
                chart.index.tiles_of(plane).filter {
                    sol.value_of(chart.lptc[ix, plane, it].to_lp()) == 1.0
                }
        }
}
