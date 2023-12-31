package kulp.variables

import kulp.*

/** Interface for variables that can be independently set to a value by the solver. */
interface LPVar<N : Number> : LPAffExpr<N>, LPBounded<N>, LPRenderable {

    /** the constraint is added directly as a child of the node of this variable. */
    context(NodeCtx)
    fun requiring(name: String, op: (BindCtx).(LPVar<N>) -> LPConstraint): LPVar<N> =
        this.also { name { op(it) } }
}
