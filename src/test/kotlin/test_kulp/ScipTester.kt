package test_kulp

import com.google.ortools.Loader
import com.google.ortools.linearsolver.MPSolver
import grid_model.GridProblem
import grid_model.adapters.lp.GridLPAdapter
import grid_model.adapters.lp.GridLPProblem
import grid_model.geom.Dim
import kulp.LPProblem
import kulp.LPSolution
import kulp.MipContext
import kulp.adapters.LPSolver
import kulp.adapters.ORToolsAdapter

/** Helper class to hide away SCIP boilerplate. */
open class ScipTester(private val dump_lp: Boolean = false) : LPSolver {
    init {
        Loader.loadNativeLibraries()
    }

    override fun LPProblem.solve(): LPSolution {
        val ctx = MipContext(10000.0)
        val solver = MPSolver.createSolver("SCIP")
        val adapter = ORToolsAdapter(solver, ctx)
        adapter.prepare(this).apply {
            if (dump_lp) println(solver.exportModelAsLpFormat())
            return execute()
        }
    }

    val <D : Dim<D>> GridProblem<D>.lp: GridLPProblem
        get() = GridLPAdapter(this).lp_prob
}
