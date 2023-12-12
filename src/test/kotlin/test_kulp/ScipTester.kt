package test_kulp

import com.google.ortools.Loader
import com.google.ortools.linearsolver.MPSolver
import kulp.LPProblem
import kulp.MipContext
import kulp.adapters.ORToolsAdapter

/** Helper class to hide away SCIP boilerplate. */
open class ScipTester {
    protected fun solve_problem(prob: LPProblem): ORToolsAdapter.ORToolsSolution {
        val ctx = MipContext(1000.0)
        Loader.loadNativeLibraries()
        val solver = MPSolver.createSolver("SCIP")
        val solution =
            solver.run {
                val adapter = ORToolsAdapter(prob, ctx)
                adapter.init()
                println(exportModelAsLpFormat())
                return@run adapter.run_solver()
            }
        return solution
    }
}
