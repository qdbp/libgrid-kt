package kulp.constraints

import kulp.LPRenderable
import model.SegName

abstract class LPConstraint : LPRenderable {

    fun constraint_identifier(): SegName = SegName(javaClass.simpleName)
}
