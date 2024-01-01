package test_kulp.test_transforms

import kulp.*
import kulp.expressions.bool_clip
import kulp.expressions.int_clip
import kulp.transforms.IntClip
import kulp.variables.LPBinary
import kulp.variables.LPInteger
import test_kulp.ScipTester
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

private class IntClipTestProblem(
    val mk_objective: (LPAffExpr<*>) -> Pair<LPAffExpr<*>, LPObjectiveSense>,
    lb: Int?,
    ub: Int?,
    val pin_x: Int? = null
) : LPProblem() {

    val x = node.bind("x") { LPInteger(pin_x, pin_x) }
    val y = node.bind("y") { IntClip(x, lb, ub) }

    override fun get_objective(): Pair<LPAffExpr<*>, LPObjectiveSense> = mk_objective(y)
}

class TestIntClip : ScipTester() {

    @Test
    fun test_clip_elision_general() {
        val root = LPNode.new_root()
        val z = root branch { "z" { LPInteger(-10, 10) } }

        var zc = root branch { z.int_clip(-5, 5) }
        assertIs<IntClip>(zc)
        assertIs<LPBinary>(zc.z_lb)
        assertIs<LPBinary>(zc.z_ub)

        // these bounds should be elided since they are redundant
        zc = root branch { z.int_clip(-15, 15) }
        assert(zc === z)

        // test elision on sum expressions
        val w = root { "w" { LPInteger(20, null) } }

        // should have inherent bound of [10,null]
        val zw = z + w
        zc = root branch { zw.int_clip(20, 30) }
        assertIs<IntClip>(zc)
        assertIs<LPBinary>(zc.z_lb)
        assertIs<LPBinary>(zc.z_ub)

        zc = root branch { zw.int_clip(10, 30) }
        assertIs<IntClip>(zc)
        // equal to inherent lower bound -- skip
        assertNull(zc.z_lb)
        assertIs<LPBinary>(zc.z_ub)

        zc = root branch { zw.int_clip(20, null) }
        assertIs<IntClip>(zc)
        assertIs<LPBinary>(zc.z_lb)
        assertNull(zc.z_ub)

        zc = root branch { zw.int_clip(10, null) }
        // clip instance skipped, but expression must be reified to integer
        assertIs<LPInteger>(zc)
        assertEquals(10, zc.lb)
        assertNull(zc.ub)
    }

    @Test
    fun test_bool_clip() {
        val root = LPNode.new_root()

        // bool clip of bool must be elided or we'll drown in aux vars
        val z = root { "z" { LPBinary() } }
        var zc = root branch { z.bool_clip() }
        assert(zc === z)

        // bool clip must be idempotent
        val x = root { "x" { LPInteger(-10, 10) } }
        zc = root branch { x.bool_clip() }
        assertIs<IntClip>(zc)
        assertIs<LPBinary>(zc.z_lb)
        assertIs<LPBinary>(zc.z_ub)

        val zc_again = root branch { zc.bool_clip() }
        // need to test nodes since reify for IntClip doesn't return the IntClip object itself
        // but rather the internal LPInteger wrapping its node.
        assertIs<LPInteger>(zc_again)
        assert(zc_again.node === zc.node)
    }

    @Test
    fun testLbOnly() {
        val prob = IntClipTestProblem({ it to LPObjectiveSense.Minimize }, lb = -10, ub = null)
        val solution = prob.solve()
        prob.node.dump_full_node_dfs()
        assertNull(prob.y.z_ub)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(1.0, solution.value_of(prob.y.z_lb!!))
        assertEquals(-10.0, solution.objective_value())
    }

    @Test
    fun testLbOnlyForcePinnedNotBound() {
        val prob =
            IntClipTestProblem({ it to LPObjectiveSense.Minimize }, lb = -10, ub = null, pin_x = 7)
        val solution = prob.solve()
        assertNull(prob.y.z_ub)
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(0.0, solution.value_of(prob.y.z_lb!!))
        assertEquals(7.0, solution.objective_value())
    }

    @Test
    fun testLbOnlyForcePinnedBound() {
        val prob =
            IntClipTestProblem(
                { it to LPObjectiveSense.Minimize },
                lb = -10,
                ub = null,
                pin_x = -15
            )
        val solution = prob.solve()
        assertNull(prob.y.z_ub)
        assertEquals(1.0, solution.value_of(prob.y.z_lb!!))
        assertEquals(-15.0, solution.value_of(prob.x))
        assertEquals(-10.0, solution.value_of(prob.y))
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(-10.0, solution.objective_value())
    }

    @Test
    fun testUbOnly() {
        val prob = IntClipTestProblem({ it to LPObjectiveSense.Maximize }, lb = null, ub = 10)
        val solution = prob.solve()
        assertEquals(solution.status(), LPSolutionStatus.Optimal)
        assertEquals(10.0, solution.objective_value())
    }

    @Test
    fun testUbOnlyForcePinnedBound() {
        val prob =
            IntClipTestProblem({ it to LPObjectiveSense.Minimize }, lb = null, ub = 10, pin_x = 20)
        val solution = prob.solve()
        assert(solution.status() == LPSolutionStatus.Optimal)
        assertNull(prob.y.z_lb)
        assertEquals(1.0, solution.value_of(prob.y.z_ub!!.node))
        assertEquals(10.0, solution.objective_value())
    }

    @Test
    fun testUbOnlyForcePinnedNotBound() {
        val prob =
            IntClipTestProblem({ it to LPObjectiveSense.Minimize }, lb = null, ub = 10, pin_x = 7)
        val solution = prob.solve()
        assert(solution.status() == LPSolutionStatus.Optimal)
        assertNull(prob.y.z_lb)
        assertEquals(0.0, solution.value_of(prob.y.z_ub!!.node))
        assertEquals(7.0, solution.objective_value())
    }

    @Test
    fun testUbLBMaximize() {
        val prob = IntClipTestProblem({ it to LPObjectiveSense.Maximize }, lb = -10, ub = 10)
        val solution = prob.solve()
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(0.0, solution.value_of(prob.y.z_lb!!.node))
        assertEquals(1.0, solution.value_of(prob.y.z_ub!!.node))
        assertEquals(10.0, solution.objective_value())
    }

    @Test
    fun testUbLBMinimize() {
        val prob = IntClipTestProblem({ it to LPObjectiveSense.Minimize }, lb = -10, ub = 10)
        val solution = prob.solve()
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(1.0, solution.value_of(prob.y.z_lb!!.node))
        assertEquals(0.0, solution.value_of(prob.y.z_ub!!.node))
        assertEquals(-10.0, solution.objective_value())
    }

    @Test
    fun testUbLBMinimizeForcePinned() {
        val prob =
            IntClipTestProblem({ it to LPObjectiveSense.Minimize }, lb = -5, ub = 5, pin_x = -20)
        val solution = prob.solve()
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(solution.value_of(prob.y.z_lb!!.node), 1.0)
        assertEquals(solution.value_of(prob.y.z_ub!!.node), 0.0)
        assertEquals(solution.objective_value(), -5.0)
    }

    @Test
    fun testUbLBMaximizeForcePinned() {
        val prob =
            IntClipTestProblem({ it to LPObjectiveSense.Maximize }, lb = -5, ub = 5, pin_x = -20)
        val solution = prob.solve()
        assertEquals(LPSolutionStatus.Optimal, solution.status())
        assertEquals(solution.value_of(prob.y.z_lb!!.node), 1.0)
        assertEquals(solution.value_of(prob.y.z_ub!!.node), 0.0)
        assertEquals(solution.objective_value(), -5.0)
    }
}
