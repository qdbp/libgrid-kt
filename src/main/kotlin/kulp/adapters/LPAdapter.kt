package kulp.adapters

import kulp.*
import kulp.variables.LPVar

/**
 * Base class for adapters that map our LPProblem to a concrete third-party backend.
 *
 * Right now this is just ORTools, but in the future we may want to support other backends.
 *
 * This lets us keep our LPProblem implementation backend-agnostic and use abstractions whose
 * implementation details do not intrude on the modelling layer.
 */
context(Solver)
abstract class LPAdapter<Solver, SolverParams>(val problem: LPProblem, val ctx: MipContext) {

    context(Solver)
    abstract fun consume_variable(variable: LPVar<*>)

    context(Solver)
    abstract fun consume_constraint(constraint: LPConstraint)

    // TODO for CP-SAT we might want to parameterize the objective expr type, to allow us to
    // require an integer objective.
    context(Solver)
    abstract fun consume_objective(objective: LPAffExpr<Double>, sense: LPObjectiveSense)

    context(Solver)
    fun init() {
        val primitives = problem.node.render(ctx)
        val already_consumed = mutableSetOf<LPNode>()

        // first, consume all variables
        val variables = primitives.values.filterIsInstance<LPVar<*>>()
        for (variable in variables) {
            println("consuming ${variable.node}")
            if (variable.node !in already_consumed) {
                consume_variable(variable)
                already_consumed.add(variable.node)
            }
        }

        // then, consume all constraints
        val constraints = primitives.values.filterIsInstance<LPConstraint>()
        for (constraint in constraints) {
            if (constraint.node !in already_consumed) {
                consume_constraint(constraint)
                already_consumed.add(constraint.node)
            }
        }

        val (obj, sense) = problem.get_objective()
        consume_objective(obj.relax(), sense)
    }

    context(Solver)
    abstract fun run_solver(params: SolverParams? = null): LPSolution
}
