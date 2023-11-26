package kulp.aggregates

import kulp.LPName
import kulp.LPRenderable
import kulp.constraints.LP_EQ
import kulp.lp_sum
import kulp.variables.LPBinary

/** Models a "one of N" constraint, where exactly one of the binary variables must be true. */
class LPOneOfN(name: LPName, vars: List<LPBinary>, shape: List<Int>) :
    LPAggregate<LPBinary>(name, vars, shape) {

    constructor(
        name: LPName,
        n: Int
    ) : this(name, (0 until n).map { LPBinary(name.refine("is_$it")) }, listOf(n))

    override fun render_interactions(): List<LPRenderable> {
        val sum = this.lp_sum()
        return listOf(LP_EQ(name.refine("sums_to_1"), sum, 1))
    }
}
