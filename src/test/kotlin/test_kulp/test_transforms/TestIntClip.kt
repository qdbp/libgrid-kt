package test_kulp.test_transforms

import com.google.ortools.Loader
import com.google.ortools.linearsolver.MPSolver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kulp.*
import kulp.adapters.ORToolsAdapter
import kulp.constraints.LP_EQ
import kulp.transforms.IntClip
import kulp.variables.LPInteger
import model.SegName

private class IntClipTestProblem(
    val mk_objective: (LPAffineExpression) -> Pair<LPExprLike, LPObjectiveSense>,
    lb: Int?,
    ub: Int?,
    val pin_x: Int? = null
) : LPProblem() {

    val x = LPInteger(SegName("x"))
    val yt = IntClip(x, lb, ub)

    override fun get_objective(): Pair<LPExprLike, LPObjectiveSense> {
        return mk_objective(yt.as_expr())
    }

    override fun get_renderables(): List<LPRenderable> {
        return listOfNotNull(x, yt, pin_x?.let { LP_EQ(x.name.refine("x_test_pin"), x, it) })
    }

    override val name: SegName
        get() = SegName("IntClipProblem")
}

class TestIntClip {
    private fun testProblem(prob: IntClipTestProblem): ORToolsAdapter.ORToolsSolution {
        // solve the whiskas problem with or-tools
        val ctx = MipContext(1000.0)
        Loader.loadNativeLibraries()
        val solver = MPSolver.createSolver("SCIP")
        val solution =
            solver.run {
                val adapter = ORToolsAdapter(prob, ctx)
                adapter.init()
                return@run adapter.run_solver()
            }
        return solution
    }

    @Test
    fun testLbOnly() {
        val prob = IntClipTestProblem({ it to LPObjectiveSense.Minimize }, lb = -10, ub = null)
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
    fun testUbOnlyForcePinned() {
        val prob =
            IntClipTestProblem({ it to LPObjectiveSense.Minimize }, lb = null, ub = 10, pin_x = 20)
        val solution = testProblem(prob)
        assert(solution.status() == LPSolutionStatus.Optimal)
        assertNull(prob.yt.z_lb)
        assertEquals(1.0, solution.value_of(prob.yt.z_ub!!.name))
        assertEquals(10.0, solution.objective_value())
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
        val prob = IntClipTestProblem({ it to LPObjectiveSense.Minimize }, lb = -5, ub = 5)
        val solution = testProblem(prob)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(1.0, solution.value_of(prob.yt.z_lb!!.name))
        assertEquals(0.0, solution.value_of(prob.yt.z_ub!!.name))
        assertEquals(-5.0, solution.objective_value())
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
}
