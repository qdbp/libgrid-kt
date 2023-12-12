package test_kulp.problems

import kulp.*
import kulp.aggregates.LPOneOfN
import kulp.expressions.RealAffExpr
import mdspan.IDX
import mdspan.SLC
import mdspan.lp_sum
import mdspan.ndindex
import org.junit.jupiter.api.Test
import test_kulp.ScipTester

private object SudokuProblem : LPProblem() {

    override fun get_objective(): Pair<LPAffExpr<*>, LPObjectiveSense> =
        Pair(RealAffExpr(0.0), LPObjectiveSense.Minimize)

    // dimensions are [row, col, digit]
    val ind_arr =
        node { "x" { LPOneOfN(shape = listOf(9, 9, 9), constraint_subspace = listOf(2)) } }.arr

    val initial_list =
        listOf(
            Triple(5, 1, 1),
            Triple(6, 2, 1),
            Triple(8, 4, 1),
            Triple(4, 5, 1),
            Triple(7, 6, 1),
            Triple(3, 1, 2),
            Triple(9, 3, 2),
            Triple(6, 7, 2),
            Triple(8, 3, 3),
            Triple(1, 2, 4),
            Triple(8, 5, 4),
            Triple(4, 8, 4),
            Triple(7, 1, 5),
            Triple(9, 2, 5),
            Triple(6, 4, 5),
            Triple(2, 6, 5),
            Triple(1, 8, 5),
            Triple(8, 9, 5),
            Triple(5, 2, 6),
            Triple(3, 5, 6),
            Triple(9, 8, 6),
            Triple(2, 7, 7),
            Triple(6, 3, 8),
            Triple(8, 7, 8),
            Triple(7, 9, 8),
            Triple(3, 4, 9),
            Triple(1, 5, 9),
            Triple(6, 6, 9),
            Triple(5, 8, 9),
        )

    // dimensions are [digit, row, col]
    init {
        node {
            // rows
            ind_arr.apply_subspace_indexed(listOf(0)) { ix, row ->
                "one_per_row_${ix.lp_name}" { row.lp_sum() eq 1 }
            }
            // columns
            ind_arr.apply_subspace_indexed(listOf(1)) { ix, col ->
                "one_per_col_${ix.lp_name}" { col.lp_sum() eq 1 }
            }

            // one per box -- no easy way to do this one!
            for ((box, digit) in ndindex(9, 9)) {
                val start_row = 3 * (box / 3)
                val start_col = 3 * (box % 3)
                val slice =
                    ind_arr.slice(
                        SLC(start_row, start_row + 3),
                        SLC(start_col, start_col + 3),
                        IDX(digit),
                    )
                "one_per_box_${digit}_${box}" { slice.lp_sum() eq 1 }
            }
            // initial values
            for ((digit, row, col) in initial_list) {
                val ind = ind_arr[row - 1, col - 1, digit - 1]
                "initial_${digit}_${row}_${col}" { (ind eq 1) }
            }
        }
    }
}

class TestSudoku : ScipTester() {
    @Test
    fun testSudoku() {
        val solution = solve_problem(SudokuProblem)
        assert(solution.status() == LPSolutionStatus.Optimal)
    }
}
