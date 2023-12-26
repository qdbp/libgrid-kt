package kulp

class ProvenInfeasible : Exception()

enum class LPSolutionStatus {
    Optimal,
    Infeasible,
    Feasible,
    Unbounded,
    Unsolved,
    Error
}
