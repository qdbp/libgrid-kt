package test_grid.level_0

import grid_model.GridProblem
import grid_model.dimension.Dim

abstract class TestGridProblem<D : Dim<D>>(dim: D) : GridProblem<D>(dim) {
    final override val name = this.javaClass.simpleName
}