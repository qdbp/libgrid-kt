package kulp

sealed class SolverType

object MIP : SolverType()

object CPSAT : SolverType()

data class MipContext(val bigM: Double = 1000.0, val solver_type: SolverType = MIP)
