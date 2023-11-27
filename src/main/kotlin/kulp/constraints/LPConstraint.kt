package kulp.constraints

import model.SegName
import kulp.LPRenderable

abstract class LPConstraint: LPRenderable {

    fun constraint_identifier(): SegName {
        return SegName("constraint_${javaClass.simpleName}")
    }
}

