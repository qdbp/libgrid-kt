package test_kulp

import kulp.LPNode
import kulp.LPProblem

abstract class TestLPProblem : LPProblem() {
    // expose root to tests
    fun root(): LPNode = node
}
