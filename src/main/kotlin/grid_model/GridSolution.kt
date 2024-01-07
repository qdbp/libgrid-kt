package grid_model

import grid_model.plane.Plane
import grid_model.tiles.Tile

interface GridSolution {

    val problem: GridProblem<*>

    fun get_entities(ix: List<Int>): Collection<Entity<*>>

    fun get_tiles(ix: List<Int>, plane: Plane): Collection<Tile>
}
