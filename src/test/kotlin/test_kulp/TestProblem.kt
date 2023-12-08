package test_kulp

import kulp.LPNode
import kulp.LPProblem

abstract class TestProblem : LPProblem() {
    // expose root to tests
    fun root(): LPNode = node
}
