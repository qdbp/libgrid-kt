package kulp.transforms

import kulp.LPExprLike
import kulp.LPRenderable
import kulp.MipContext

// TODO these don't strictly need to be "renderable", but they must be breakable into
//  renderables
/**
 * Class representing transformations of individual or groups of variables.
 */
abstract class LPTransform : LPExprLike, LPRenderable {

    protected fun transform_identifier(): String {
        return "transform_${javaClass.simpleName}"
    }

    override fun is_primitive(ctx: MipContext): Boolean = false

}
