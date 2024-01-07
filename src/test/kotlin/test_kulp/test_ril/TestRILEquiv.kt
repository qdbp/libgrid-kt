package test_kulp.test_ril

import kulp.LPAffExpr
import kulp.branch
import kulp.expressions.Zero
import kulp.ril.RIL
import test_kulp.*
import kotlin.test.Test

private class EquivRILProblem(mk_objective: (LPAffExpr<Int>, LPAffExpr<Int>) -> LPAffExpr<Int>) :
    RILProblem(mk_objective) {

    override val witness = node { RIL.equiv(x, y) }
}

class TestRILEquiv : ScipTester(dump_lp = true) {

    @Test
    fun witnesses_true() {
        EquivRILProblem { x, y -> x + y }
            .run {
                solve().run {
                    assertObjective(20.0)
                    assertRilTrue(witness)
                }
            }
    }

    @Test
    fun witnesses_false() {
        EquivRILProblem { x, y -> x - y }
            .run {
                solve().run {
                    assertObjective(20.0)
                    assertRilFalse(witness)
                }
            }
    }

    @Test
    fun binds_feasible_pos() {
        EquivRILProblem { x, y -> x * 2 - y }
            .run {
                branch { "test_bind_pin" { witness ge 1 } }
                solve().run {
                    assertObjective(19.0)
                    assertValue(10.0, x)
                    assertValue(1.0, y)
                    assertRilTrue(witness)
                }
            }
    }

    @Test
    fun binds_feasible_neg() {
        EquivRILProblem { x, y -> x * 2 + y }
            .run {
                branch { "test_bind_pin" { witness le 0 } }
                solve().run {
                    assertObjective(20.0)
                    assertValue(10.0, x)
                    assertValue(0.0, y)
                    assertRilFalse(witness)
                }
            }
    }

    @Test
    fun binds_infeasible_neg() {
        EquivRILProblem { x, y -> Zero }
            .run {
                branch {
                    "test_bind_pin" { witness le 0 }
                    "pin_x" { x eq 5 }
                    "pin_y" { y eq 5 }
                }
                solve().run { assertInfeasible() }
            }
    }

    @Test
    fun binds_infeasible_pos() {
        EquivRILProblem { x, y -> Zero }
            .run {
                branch {
                    "test_bind_pin" { witness ge 1 }
                    "pin_x" { x eq 5 }
                    "pin_y" { y eq -5 }
                }
                solve().run { assertInfeasible() }
            }
    }
}
