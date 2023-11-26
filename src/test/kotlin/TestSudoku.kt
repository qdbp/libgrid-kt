import com.google.ortools.Loader
import com.google.ortools.linearsolver.MPSolver
import kulp.*
import kulp.adapters.ORToolsAdapter
import kulp.constraints.LP_EQ
import kulp.variables.LPBinary
import kulp.variables.LPVariable
import mdspan.*
import org.junit.jupiter.api.Test

private object SudokuProblem : LPProblem() {

    val variables: Map<LPName, LPVariable> =
        ndindex(9, 9, 9).associate {
            val name = "z".lpn.refine(it[0], it[1], it[2])
            name to LPBinary(name)
        }

    override fun get_objective(): Pair<LPExprLike, LPObjectiveSense> {
        return Pair(LPAffineExpression(), LPObjectiveSense.Minimize)
    }

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

    override fun get_renderables(): List<LPRenderable> {
        // dimensions are [digit, row, col]
        val span = variables.values.toList().mdspan(9, 9, 9)
        val renderables = mutableListOf<LPRenderable>()

        // exclusivity constraint
        for ((row, col) in ndindex(9, 9)) {
            val slice = span[ALL, IX(row), IX(col)]
            renderables.add(LP_EQ("exactly_one_at".lpn.refine(row, col), slice.lp_sum(), 1))
        }
        // one per col
        for ((col, digit) in ndindex(9, 9)) {
            val slice = span[IX(digit), ALL, IX(digit)]
            renderables.add(LP_EQ("one_per_col".lpn.refine(digit, col), slice.lp_sum(), 1))
        }
        // one per row
        for ((row, digit) in ndindex(9, 9)) {
            val slice = span[IX(digit), IX(row), ALL]
            renderables.add(LP_EQ("one_per_row".lpn.refine(digit, row), slice.lp_sum(), 1))
        }
        // one per box
        for ((box, digit) in ndindex(9, 9)) {
            val start_row = 3 * (box / 3)
            val start_col = 3 * (box % 3)
            val slice = span[IX(digit), SL(start_row, start_row + 3), SL(start_col, start_col + 3)]
            renderables.add(LP_EQ("one_per_box".lpn.refine(digit, box), slice.lp_sum(), 1))
        }

        for ((digit, row, col) in initial_list) {
            renderables.add(LP_EQ("initial".lpn.refine(digit, row, col), span[digit - 1, row - 1, col - 1], 1))
        }

        renderables.addAll(variables.values)
        return renderables
    }

    override val name: LPName = "SudokuProblem".lpn
}

class TestSudoku {
    @Test
    fun testSudoku() {
        // solve the whiskas problem with or-tools
        val ctx = MipContext(1000.0)
        Loader.loadNativeLibraries()
        val solver = MPSolver.createSolver("SCIP")
        val solution =
            solver.run {
                val adapter = ORToolsAdapter(SudokuProblem, ctx)
                adapter.init()
                return@run adapter.run_solver()
            }
        assert(solution.status() == LPSolutionStatus.Optimal)
    }
}
