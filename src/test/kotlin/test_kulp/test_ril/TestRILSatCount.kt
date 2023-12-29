package test_kulp.test_ril

import kulp.LPObjective
import kulp.LPObjectiveSense
import kulp.lp_dot
import kulp.transforms.ril.RIL
import kulp.variables.LPInteger
import test_kulp.ScipTester
import test_kulp.TestLPProblem
import test_kulp.assertInfeasible
import test_kulp.assertObjective
import kotlin.test.Test

private class SatCountRILProblem(val minimize: Boolean = false) : TestLPProblem() {

    val vars = node { "abc".toList().bind_each("var") { LPInteger(-1, 1) } }

    override fun get_objective(): LPObjective =
        vars.lp_dot(1..3) to if (minimize) LPObjectiveSense.Minimize else LPObjectiveSense.Maximize

    val min_witness_0 = node.branch("min_0") { RIL.min_sat(0, vars) }
    val min_witness_1 = node.branch("min_1") { RIL.min_sat(1, vars) }
    val min_witness_2 = node.branch("min_2") { RIL.min_sat(2, vars) }
    val min_witness_3 = node.branch("min_3") { RIL.min_sat(3, vars) }

    val max_witness_0 = node.branch("max_0") { RIL.max_sat(0, vars) }
    val max_witness_1 = node.branch("max_1") { RIL.max_sat(1, vars) }
    val max_witness_2 = node.branch("max_2") { RIL.max_sat(2, vars) }
    val max_witness_3 = node.branch("max_3") { RIL.max_sat(3, vars) }
}

class TestRILSatCount : ScipTester() {

    @Test
    fun test_witnesses_zero_set() {
        val prob = SatCountRILProblem()
        with(prob) {
            node { vars.bind_each("pin") { it eq 0 } }
            with(solve(prob)) {
                assertRilTrue(min_witness_0)
                assertRilFalse(min_witness_1)
                assertRilFalse(min_witness_2)
                assertRilFalse(min_witness_3)
                assertRilTrue(max_witness_0)
                assertRilTrue(max_witness_1)
                assertRilTrue(max_witness_2)
                assertRilTrue(max_witness_3)
            }
        }
    }

    @Test
    fun test_witnesses_one_set() {
        val prob = SatCountRILProblem()
        with(prob) {
            node {
                vars.take(1).bind_each("pin_true") { it eq 1 }
                vars.drop(1).bind_each("pin_false") { it eq 0 }
            }
            with(solve(prob)) {
                assertRilTrue(min_witness_0)
                assertRilTrue(min_witness_1)
                assertRilFalse(min_witness_2)
                assertRilFalse(min_witness_3)
                assertRilFalse(max_witness_0)
                assertRilTrue(max_witness_1)
                assertRilTrue(max_witness_2)
                assertRilTrue(max_witness_3)
            }
        }
    }

    @Test
    fun test_witnesses_two_set() {
        val prob = SatCountRILProblem()
        with(prob) {
            node {
                vars.take(2).bind_each("pin_true") { it eq 1 }
                vars.drop(2).bind_each("pin_false") { it eq 0 }
            }
            with(solve(prob)) {
                assertRilTrue(min_witness_0)
                assertRilTrue(min_witness_1)
                assertRilTrue(min_witness_2)
                assertRilFalse(min_witness_3)
                assertRilFalse(max_witness_0)
                assertRilFalse(max_witness_1)
                assertRilTrue(max_witness_2)
                assertRilTrue(max_witness_3)
            }
        }
    }

    @Test
    fun test_witnesses_three_set() {
        val prob = SatCountRILProblem()
        with(prob) {
            node { vars.bind_each("pin") { it eq 1 } }
            with(solve(prob)) {
                assertRilTrue(min_witness_0)
                assertRilTrue(min_witness_1)
                assertRilTrue(min_witness_2)
                assertRilTrue(min_witness_3)
                assertRilFalse(max_witness_0)
                assertRilFalse(max_witness_1)
                assertRilFalse(max_witness_2)
                assertRilTrue(max_witness_3)
                // double check we have the objective right for subsequent tests
                assertObjective(6.0)
            }
        }
    }

    @Test
    fun test_binds_max_feasible() {
        var prob = SatCountRILProblem()
        with(prob) {
            node { "max_sat_pin" { RIL.max_sat(1, vars).is_ril_true } }
            // best solution = set last; zero first two
            with(solve(prob)) { assertObjective(3.0) }
        }

        prob = SatCountRILProblem()
        with(prob) {
            node {
                vars.bind_each("pin") { it ge 0 }
                "max_sat_pin" { RIL.max_sat(2, vars).is_ril_true }
            }
            // best solution = set last two; zero first
            with(solve(prob)) { assertObjective(5.0) }
        }
    }

    @Test
    fun test_binds_max_infeasible() {
        val prob = SatCountRILProblem()
        with(prob) {
            node {
                vars.bind_each("pin") { it ge 1 }
                "max_sat_pin" { RIL.max_sat(1, vars).is_ril_true }
            }
            with(solve(prob)) { assertInfeasible() }
        }
    }

    @Test
    fun test_binds_min_feasible() {
        var prob = SatCountRILProblem(minimize = true)
        with(prob) {
            node { "min_sat_pin" { RIL.min_sat(1, vars).is_ril_true } }
            // + 1 (pins cheapest) -2 -3
            with(solve(prob)) { assertObjective(-4.0) }
        }

        prob = SatCountRILProblem(minimize = true)
        with(prob) {
            node { "min_sat_pin" { RIL.min_sat(3, vars).is_ril_true } }
            with(solve(prob)) { assertObjective(6.0) }
        }
    }

    @Test
    fun test_binds_min_infeasible() {
        val prob = SatCountRILProblem(minimize = true)
        with(prob) {
            node {
                vars.bind_each("pin") { it le 0 }
                "min_sat_pin" { RIL.min_sat(1, vars).is_ril_true }
            }
            with(solve(prob)) { assertInfeasible() }
        }
    }
}
