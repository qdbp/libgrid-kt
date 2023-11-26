package kulp.adapters

import com.google.ortools.linearsolver.MPConstraint
import com.google.ortools.linearsolver.MPObjective
import com.google.ortools.linearsolver.MPSolver
import com.google.ortools.linearsolver.MPSolver.ResultStatus
import com.google.ortools.linearsolver.MPVariable
import kulp.*
import kulp.constraints.LPConstraint
import kulp.constraints.LP_LEQ
import kulp.variables.*

private val logger = io.github.oshai.kotlinlogging.KotlinLogging.logger {}

context(MPSolver)
class ORToolsAdapter(problem: LPProblem, ctx: MipContext) :
    LPAdapter<MPSolver, Unit>(problem, ctx) {

    class ORToolsSolution(
        private val objective_value: Double?,
        private val solved_value_map: Map<LPName, Double>,
        private val ortools_status: ResultStatus
    ) : LPSolution() {
        override fun status(): LPSolutionStatus {
            return when (ortools_status) {
                ResultStatus.OPTIMAL -> LPSolutionStatus.Optimal
                ResultStatus.FEASIBLE -> LPSolutionStatus.Feasible
                ResultStatus.INFEASIBLE -> LPSolutionStatus.Infeasible
                ResultStatus.UNBOUNDED -> LPSolutionStatus.Unbounded
                ResultStatus.ABNORMAL -> LPSolutionStatus.Error
                ResultStatus.NOT_SOLVED -> LPSolutionStatus.Unsolved
                else -> LPSolutionStatus.Error
            }
        }

        override fun objective_value(): Double {
            return objective_value
                ?: throw Exception("Objective value not available. Check status first!")
        }

        override fun value_of(name: LPName): Double? {
            return solved_value_map[name]
        }

        override fun toString(): String {
            return "ORToolsSolution(objective_value=$objective_value, ortools_status=$ortools_status)"
        }
    }

    private val known_variables = mutableMapOf<LPName, Pair<MPVariable, LPVariable>>()
    private val known_constraints = mutableMapOf<LPName, Pair<MPConstraint, LPConstraint>>()
    private var objective: MPObjective? = null

    context(MPSolver)
    override fun consume_variable(variable: LPVariable) {
        // right now we skip duplicate variables, because they might be rendered multipled times
        // with this not necessarily indicating an error. However, we might want to add a strict
        // mode?
        if (variable.name in known_variables) {
            return
        }
        val ortools_name = variable.name.render()
        val ortools_var =
            when (variable) {
                // note: we skip intrinsic constraints for these types because we can express them
                is LPReal -> makeNumVar(-MPSolver.infinity(), MPSolver.infinity(), ortools_name)
                is LPInteger -> makeIntVar(-MPSolver.infinity(), MPSolver.infinity(), ortools_name)
                is LPNonnegativeInteger -> makeIntVar(0.0, MPSolver.infinity(), ortools_name)
                is LPBinary -> makeIntVar(0.0, 1.0, ortools_name)
                // for anything else we make a "generic" variable and trust that the intrinsics
                // have been added during the rendering pass!
                else ->
                    when (variable.domain) {
                        LPDomain.Real ->
                            makeNumVar(-MPSolver.infinity(), MPSolver.infinity(), ortools_name)
                        LPDomain.Integral ->
                            makeIntVar(-MPSolver.infinity(), MPSolver.infinity(), ortools_name)
                    }
            }
        known_variables[variable.name] = Pair(ortools_var, variable)
    }

    context(MPSolver)
    override fun consume_constraint(constraint: LPConstraint) {
        if (constraint.name in known_constraints) {
            return
        }
        val ortools_name = constraint.name.render()
        val ortools_constraint =
            when (constraint) {
                is LP_LEQ -> {
                    makeConstraint(MPSolver.infinity(), MPSolver.infinity(), ortools_name)
                        .apply {
                            for ((variable, coef) in constraint.std_lhs.terms) {
                                known_variables[variable.name]?.let {
                                    setCoefficient(it.first, coef)
                                }
                                    ?: logger.warn {
                                        "Unknown variable ${variable.name} in constraint. Bug?? Skipping."
                                    }
                            }
                            setBounds(-MPSolver.infinity(), -constraint.std_lhs.constant)
                            known_constraints[constraint.name] = Pair(this, constraint)
                        }
                }
                else ->
                    throw Exception(
                        "Passed unreduced (non-LEQ) constraint to ORToolsAdapter. Bug??")
            }
        known_constraints[constraint.name] = Pair(ortools_constraint, constraint)
    }

    context(MPSolver)
    override fun consume_objective(objective: LPAffineExpression, sense: LPObjectiveSense) {
        val ortools_objective = objective()
        for (term in objective.terms.entries) {
            known_variables[term.key.name]?.let {
                ortools_objective.setCoefficient(it.first, term.value)
            } ?: logger.warn { "Unknown variable ${term.key.name} in objective. Bug?? Skipping." }
        }
        when (sense) {
            LPObjectiveSense.Minimize -> ortools_objective.setMinimization()
            LPObjectiveSense.Maximize -> ortools_objective.setMaximization()
        }
        this.objective = ortools_objective
    }

    context(MPSolver)
    override fun run_solver(params: Unit?): ORToolsSolution {
        // TODO implement params properly

        val result = solve()
        val value_map =
            known_variables
                .map { Pair(it.value.second.name, it.value.first.solutionValue()) }
                .toMap()
        return ORToolsSolution(
            objective_value = objective?.value(),
            solved_value_map = value_map,
            ortools_status = result)
    }
}
