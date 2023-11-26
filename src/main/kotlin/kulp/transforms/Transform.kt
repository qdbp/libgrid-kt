package kulp.transforms

import kulp.LPExprLike
import kulp.LPRenderable

// TODO these don't strictly need to be "renderable", but they must be breakable into
//  renderables
/**
 * Class representing transformations of individual or groups of variables.
 */
abstract class Transform : LPExprLike, LPRenderable {

    protected fun transform_identifier(): String {
        return "transform_${javaClass.simpleName}"
    }

}
