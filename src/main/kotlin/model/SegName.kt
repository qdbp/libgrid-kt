package model

const val LP_NAMESPACE_SEP = "::"

val String.sn
    get() = SegName(this)

/**
 * A Segmented Name: a wrapper around basic strings that allows for easy definition of hierarchies
 * of related names.
 */
class SegName(private val segments: List<String>) {
    constructor(vararg segments: String) : this(segments.toList())

    fun refine(vararg s: String): SegName {
        return SegName(segments + s.toList())
    }

    fun refine(other: SegName): SegName {
        return SegName(segments + other.segments)
    }

    fun refine(vararg n: Int): SegName {
        return refine("(" + n.joinToString(",") + ")")
    }

    fun refine(n: List<Int>): SegName {
        return refine("(" + n.joinToString(",") + ")")
    }

    fun render(separator: String = LP_NAMESPACE_SEP): String = segments.joinToString(separator)

    override fun toString(): String = render()

    override fun hashCode(): Int {
        return segments.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is SegName) return false
        return segments == other.segments
    }
}
