package kulp.aggregates

import kulp.LPAggregate
import kulp.LPRenderable
import kulp.constraints.LP_EQ
import kulp.lp_sum
import kulp.variables.LPBinary
import mdspan.prod
import model.LPName

/**
 * The archetypal aggregate!
 *
 * This class models the fundamental "one of N" constraint, where exactly one of the binary
 * variables must be true.
 */
class LPOneOfN(name: LPName, vars: List<LPBinary>, shape: List<Int>) :
    LPAggregate<LPBinary>(name, vars, shape) {

    constructor(
        name: LPName,
        n: Int
    ) : this(name, (0 until n).map { LPBinary(name.refine("is_$it")) }, listOf(n))

    constructor(
        name: LPName,
        shape: List<Int>
    ) : this(name, List(shape.prod()) { ix -> LPBinary(name.refine(ix)) }, shape)

    override fun LPName.render_interactions(): List<LPRenderable> {
        val sum = this@LPOneOfN.lp_sum()
        return listOf(LP_EQ(+"sums_to_1", sum, 1))
    }
}
