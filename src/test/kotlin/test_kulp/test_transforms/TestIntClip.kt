package test_kulp.test_transforms

import com.google.ortools.Loader
import com.google.ortools.linearsolver.MPSolver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kulp.*
import kulp.adapters.ORToolsAdapter
import kulp.transforms.IntClip
import kulp.variables.LPInteger
import model.SegName

private class IntClipTestProblem(
    val mk_objective: (LPAffExpr<*>) -> Pair<LPAffExpr<*>, LPObjectiveSense>,
    lb: Int?,
    ub: Int?,
    val pin_x: Int? = null
) : LPProblem() {

    val x = LPInteger(SegName("x"), pin_x, pin_x)
    val yt = IntClip(x, lb, ub)

    override fun get_objective(): Pair<LPAffExpr<*>, LPObjectiveSense> {
        return mk_objective(yt)
    }

    override fun get_renderables(): List<LPRenderable> {
        return listOfNotNull(x, yt)
    }

    override val name: SegName
        get() = SegName("IntClipProblem")
}

class TestIntClip {
    private fun testProblem(prob: IntClipTestProblem): ORToolsAdapter.ORToolsSolution {
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

    @Test
    fun testLbOnly() {
        val prob = IntClipTestProblem({ it to LPObjectiveSense.Minimize }, lb = -10, ub = null)
        val solution = testProblem(prob)
        assertNull(prob.yt.z_ub)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(1.0, solution.value_of(prob.yt.z_lb!!.name))
        assertEquals(-10.0, solution.objective_value())
    }

    @Test
    fun testLbOnlyForcePinnedNotBound() {
        val prob =
            IntClipTestProblem({ it to LPObjectiveSense.Minimize }, lb = -10, ub = null, pin_x = 7)
        val solution = testProblem(prob)
        assertNull(prob.yt.z_ub)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(0.0, solution.value_of(prob.yt.z_lb!!.name))
        assertEquals(7.0, solution.objective_value())
    }

    @Test
    fun testLbOnlyForcePinnedBound() {
        val prob =
            IntClipTestProblem({ it to LPObjectiveSense.Minimize }, lb = -10, ub = null, pin_x = -15)
        val solution = testProblem(prob)
        assertNull(prob.yt.z_ub)
        assertEquals(1.0, solution.value_of(prob.yt.z_lb!!.name))
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(-10.0, solution.objective_value())
    }


    @Test
    fun testUbOnly() {
        val prob = IntClipTestProblem({ it to LPObjectiveSense.Maximize }, lb = null, ub = 10)
        val solution = testProblem(prob)
        assertEquals(solution.status(), LPSolutionStatus.Optimal)
        assertEquals(10.0, solution.objective_value())
    }

    @Test
    fun testUbOnlyForcePinnedBound() {
        val prob =
            IntClipTestProblem({ it to LPObjectiveSense.Minimize }, lb = null, ub = 10, pin_x = 20)
        val solution = testProblem(prob)
        assert(solution.status() == LPSolutionStatus.Optimal)
        assertNull(prob.yt.z_lb)
        assertEquals(1.0, solution.value_of(prob.yt.z_ub!!.name))
        assertEquals(10.0, solution.objective_value())
    }

    @Test
    fun testUbOnlyForcePinnedNotBound() {
        val prob =
            IntClipTestProblem({ it to LPObjectiveSense.Minimize }, lb = null, ub = 10, pin_x = 7)
        val solution = testProblem(prob)
        assert(solution.status() == LPSolutionStatus.Optimal)
        assertNull(prob.yt.z_lb)
        assertEquals(0.0, solution.value_of(prob.yt.z_ub!!.name))
        assertEquals(7.0, solution.objective_value())
    }

    @Test
    fun testUbLBMaximize() {
        val prob = IntClipTestProblem({ it to LPObjectiveSense.Maximize }, lb = -10, ub = 10)
        val solution = testProblem(prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(0.0, solution.value_of(prob.yt.z_lb!!.name))
        assertEquals(1.0, solution.value_of(prob.yt.z_ub!!.name))
        assertEquals(10.0, solution.objective_value())
    }

    @Test
    fun testUbLBMinimize() {
        val prob = IntClipTestProblem({ it to LPObjectiveSense.Minimize }, lb = -10, ub = 10)
        val solution = testProblem(prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(1.0, solution.value_of(prob.yt.z_lb!!.name))
        assertEquals(0.0, solution.value_of(prob.yt.z_ub!!.name))
        assertEquals(-10.0, solution.objective_value())
    }

    @Test
    fun testUbLBMinimizeForcePinned() {
        val prob =
            IntClipTestProblem({ it to LPObjectiveSense.Minimize }, lb = -5, ub = 5, pin_x = -20)
        val solution = testProblem(prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(solution.value_of(prob.yt.z_lb!!.name), 1.0)
        assertEquals(solution.value_of(prob.yt.z_ub!!.name), 0.0)
        assertEquals(solution.objective_value(), -5.0)
    }

    @Test
    fun testUbLBMaximizeForcePinned() {
        val prob =
            IntClipTestProblem({ it to LPObjectiveSense.Maximize }, lb = -5, ub = 5, pin_x = -20)
        val solution = testProblem(prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(solution.value_of(prob.yt.z_lb!!.name), 1.0)
        assertEquals(solution.value_of(prob.yt.z_ub!!.name), 0.0)
        assertEquals(solution.objective_value(), -5.0)
    }
}
