package kulp.variables

import kulp.*

/**
 * Interface for primitive variables that have a simple representation in the output model
 * - a single variable name with no auxiliaries
 * - at most a simple upper bound and a simple lower bound constraint
 * - a domain constraint
 *
 *   AKA what you think of as a variable when you read mathematical
 */
abstract class LPVar<N : Number>(
    node: LPNode,
    override val domain: LPDomain<N>,
) : LPBounded<N>, LPAffExpr<N> by domain.unsafe_node_as_expr(node), LPRenderable(node) {

    /** the constraint is added directly as a child of the node of this variable. */
    infix fun requiring(op: (LPVar<N>) -> Attachable<LPConstraint>): LPVar<N> {
        node += op(this)
        return this
    }
}
