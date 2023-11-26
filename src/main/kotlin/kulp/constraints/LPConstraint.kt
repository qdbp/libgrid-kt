package kulp.constraints

import kulp.LPName
import kulp.LPRenderable

abstract class LPConstraint: LPRenderable {

    fun constraint_identifier(): LPName {
        return LPName("constraint_${javaClass.simpleName}")
    }
}

