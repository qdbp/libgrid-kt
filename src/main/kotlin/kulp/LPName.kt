package kulp

const val LP_NAMESPACE_SEP = "::"

val String.lpn
    get() = LPName(this)

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

    fun render(separator: String = LP_NAMESPACE_SEP): String = segments.joinToString(separator)

    override fun toString(): String = render()

    override fun hashCode(): Int {
        return segments.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is LPName) return false
        return segments == other.segments
    }
}
