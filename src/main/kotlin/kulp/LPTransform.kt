package kulp

import kulp.variables.PrimitiveLPVariable

/**
 * Class representing transformations of individual or groups of variables.
 *
 * The output of each transformation must be embodied by a single variable, which is constrained by
 * some function of the inputs, as defined by the type of transform.
 *
 * Examples of transformations include:
 * - max/min of a set of variables
 * - absolute value of a variable
 * - clipping a variable to a range
 *
 * Avoid writing any transformations that can be expressed as simple affine expressions and
 * constraints over those, since that will be more efficient.
 *
 * You must obey the Fundamental Dogma of Transformations:
 * !! a transformation never constrains its inputs !!
 * Only the output variable is constrained.
 *
 * The sole exception to this is the `Constrained` pseudo-transform, which is more of a container
 * for pre-existing constraints.
 *
 */
abstract class LPTransform<N : Number>(
    /** The output variable of the transformation. */
    protected val output: PrimitiveLPVariable<N>
) : LPVariable<N> by output {

    override fun is_primitive(ctx: MipContext): Boolean = false

    final override fun render(ctx: MipContext): List<LPRenderable> {
        return render_auxiliaries(ctx) + output
    }

    /**
     * Returns a list of auxiliary variables and constraints that are required to represent this
     * transformation.
     */
    abstract fun render_auxiliaries(ctx: MipContext): List<LPRenderable>
}
