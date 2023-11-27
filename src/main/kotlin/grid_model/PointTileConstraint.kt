package grid_model

/**
 * All constraints must reduce to the following normal form for tiles:
 * - OR ( (tile1 XOR tile2 XOR ...) AND (tile3 XOR tile4 XOR ...) AND ... ... )
 *
 * This models each point in the grid as satisfying at least one set of properties that consist of
 * each of a number of planes having a particular tile at that point.
 */
sealed class PrimitiveTileConstraint : PointTileConstraint {
    fun at_local(coord: List<Int>): LocalTileConstraint {
        return LocalTileConstraint(coord, this)
    }
}

data class IsTile(val tile: Tile) : PrimitiveTileConstraint() {
    override fun reduce(): OrConstraint {
        return OrConstraint(listOf(AndConstraint(listOf(XorConstraint(listOf(this))))))
    }
}

data class XorConstraint(val constraints: List<IsTile>) : PrimitiveTileConstraint() {
    constructor(tile: Tile) : this(listOf(IsTile(tile)))

    override fun reduce(): OrConstraint {
        return OrConstraint(listOf(AndConstraint(listOf(this))))
    }
}

data class AndConstraint(val constraints: List<XorConstraint>) : PrimitiveTileConstraint() {
    constructor(constraint: XorConstraint) : this(listOf(constraint))

    override fun reduce(): OrConstraint {
        return OrConstraint(listOf(this))
    }
}

data class OrConstraint(val constraints: List<AndConstraint>) : PrimitiveTileConstraint() {
    constructor(constraint: AndConstraint) : this(listOf(constraint))

    override fun reduce(): OrConstraint = this
}

interface PointTileConstraint {
    fun reduce(): OrConstraint
}
