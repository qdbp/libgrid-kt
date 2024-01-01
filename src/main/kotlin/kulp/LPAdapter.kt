package kulp

import kulp.adapters.LPSolver
import kulp.variables.LPVar

/**
 * Base class for adapters that map our LPProblem to a concrete third-party backend.
 *
 * Right now this is just ORTools, but in the future we may want to support other backends.
 *
 * This lets us keep our LPProblem implementation backend-agnostic and use abstractions whose
 * implementation details do not intrude on the modelling layer.
 */
abstract class LPAdapter<Solver>(private val solver: Solver, val ctx: MipContext) : LPSolver {

    context(Solver)
    abstract fun consume_variable(variable: LPVar<*>)

    context(Solver)
    abstract fun consume_constraint(constraint: LPConstraint)

    // TODO for CP-SAT we might want to parameterize the objective expr type, to allow us to
    // require an integer objective.
    context(Solver)
    abstract fun consume_objective(objective: LPAffExpr<Double>, sense: LPObjectiveSense)

    context(Solver)
    abstract fun execute_solver(): LPSolution

    inner class SolutionExecutor(val solver: Solver) {
        fun execute(): LPSolution = solver.run { execute_solver() }
    }

    // often we want to prepare a solver and perhaps perform some manipulations on it before
    // executing it. This lets us do that.
    fun prepare(prob: LPProblem): SolutionExecutor {
        solver.run {
            val primitives = prob.node.render(ctx)
            val already_consumed = mutableSetOf<LPPath>()

            // first, consume all variables
            val variables = primitives.values.filterIsInstance<LPVar<*>>()

            for (variable in variables) {
                if (variable.path !in already_consumed) {
                    consume_variable(variable)
                    already_consumed.add(variable.path)
                }
            }

            // then, consume all constraints
            val constraints = primitives.values.filterIsInstance<LPConstraint>()
            for (constraint in constraints) {
                if (constraint.path !in already_consumed) {
                    consume_constraint(constraint)
                    already_consumed.add(constraint.path)
                }
            }

            val (obj, sense) = prob.get_objective()
            consume_objective(obj.relax(), sense)

            return SolutionExecutor(this)
        }
    }

    override fun LPProblem.solve(): LPSolution = prepare(this).execute()
}
