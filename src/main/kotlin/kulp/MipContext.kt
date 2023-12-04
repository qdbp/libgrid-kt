package kulp

import kotlin.math.roundToInt

sealed class SolverType

object MIP : SolverType()

object CPSAT : SolverType()

data class MipContext(val bigM: Double = 1000.0, val solver_type: SolverType = MIP) {
    val intM = bigM.roundToInt()
}
