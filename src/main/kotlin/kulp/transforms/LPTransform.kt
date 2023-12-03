package kulp.transforms

import kulp.LPAffExpr
import kulp.LPRenderable
import kulp.MipContext
import kulp.variables.LPVariable
import model.SegName

// TODO these don't strictly need to be "renderable", but they must be breakable into
//  renderables
/**
 * Class representing transformations of individual or groups of variables.
 *
 * Each transformation must reduce to a single variable, which is constrained to equal to the
 * transformed value. The inputs can be any number of variables.
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
    val output: LPVariable<N>
) : LPAffExpr<N> by output, LPRenderable {

    final override val name: SegName = output.name.refine("aux")

    override fun is_primitive(ctx: MipContext): Boolean = false
}
