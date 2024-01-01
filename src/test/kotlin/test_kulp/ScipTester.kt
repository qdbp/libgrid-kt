package test_kulp

import com.google.ortools.Loader
import com.google.ortools.linearsolver.MPSolver
import grid_model.GridProblem
import grid_model.adapters.lp.GridLPAdapter
import grid_model.dimension.Dim
import kulp.LPProblem
import kulp.LPSolution
import kulp.MipContext
import kulp.adapters.LPSolver
import kulp.adapters.ORToolsAdapter

/** Helper class to hide away SCIP boilerplate. */
open class ScipTester(private val dump_lp: Boolean = false) : LPSolver {
    override fun LPProblem.solve(): LPSolution {
        val ctx = MipContext(10000.0)
        Loader.loadNativeLibraries()
        val solver = MPSolver.createSolver("SCIP")
        val adapter = ORToolsAdapter(solver, ctx)
        adapter.prepare(this).apply {
            if (dump_lp) println(solver.exportModelAsLpFormat())
            return execute()
        }
    }

    // convenience methods for adapters
    val <D : Dim<D>> GridProblem<D>.lp: LPProblem
        get() = GridLPAdapter(this).lp_prob
}
