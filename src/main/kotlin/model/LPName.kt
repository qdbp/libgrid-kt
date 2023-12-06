package model

import kulp.LPRenderable
import kulp.UnboundRenderable

const val LP_NAMESPACE_SEP = "::"

val String.sn
    get() = LPName(this)

/** A Segmented Name: a wrapper around a basic "path-like" segmented object name. */
class LPName(private val segments: List<String>) {
    constructor(vararg segments: String) : this(segments.toList())

    fun refine(vararg s: String): LPName {
        return LPName(segments + s.toList())
    }

    fun refine(other: LPName): LPName {
        return LPName(segments + other.segments)
    }

    fun refine(vararg n: Int): LPName {
        return refine("(" + n.joinToString(",") + ")")
    }

    fun refine(n: List<Int>): LPName {
        return refine("(" + n.joinToString(",") + ")")
    }

    fun render(separator: String = LP_NAMESPACE_SEP): String = segments.joinToString(separator)

    operator fun String.unaryPlus(): LPName {
        return this@LPName.refine(this)
    }

    operator fun List<Int>.unaryPlus(): LPName {
        return this@LPName.refine(this)
    }

    override fun toString(): String = render()

    override fun hashCode(): Int {
        return segments.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is LPName) return false
        return segments == other.segments
    }

    infix fun lp_bind(rnd: UnboundRenderable<*>): LPRenderable {
        return rnd.bind(this)
    }
}

context(LPName)
infix fun String.lp_bind(rnd: UnboundRenderable<*>): LPRenderable {
    return this@LPName.refine(this).lp_bind(rnd)

}
