package grid_model

import grid_model.planes.Plane

interface GridSolution {

    val problem: GridProblem<*>

    fun get_entities(ix: List<Int>): Collection<Entity<*>>

    fun get_tiles(ix: List<Int>, plane: Plane): Collection<Tile>
}
