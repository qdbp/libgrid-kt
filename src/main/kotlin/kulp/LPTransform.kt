package kulp

import kulp.variables.LPVariable

// TODO these don't strictly need to be "renderable", but they must be breakable into
//  renderables
/**
 * Class representing transformations of individual or groups of variables.
 *
 * The output of each transformation must be embodied by a single variable, which is constrained
 * by some function of the inputs, as defined by the type of transform.
 *
 * Examples of transformations include:
 * - max/min of a set of variables
 * - absolute value of a variable
 * - clipping a variable to a range
 *
 * Avoid writing any transformations that can be expressed as simple affine expressions and
 * constraints over those, since that will be more efficient.
 */
abstract class LPTransform<N : Number>(
    /** The output variable of the transformation. */
    private val output: LPVariable<N>
) : LPVariable<N> by output {

    override fun is_primitive(ctx: MipContext): Boolean = false

    final override fun render(ctx: MipContext): List<LPRenderable> {
        // subtlety: we do not call super here, because that would add `this` to the list of
        //  renderables, which would cause an infinite loop.
        // Though this object represents the same variable as `output`, the semantics of the
        // *object* are different, and we need to be careful with them.
        return render_auxiliaries(ctx) + output
    }

    /**
     * Returns a list of auxiliary variables and constraints that are required to represent this
     * transformation.
     */
    abstract fun render_auxiliaries(ctx: MipContext): List<LPRenderable>
}
