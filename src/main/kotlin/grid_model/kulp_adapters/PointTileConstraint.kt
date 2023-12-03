package grid_model.kulp_adapters

import grid_model.Tile
import kulp.LPRenderable
import kulp.MipContext
import kulp.variables.LPBinary

/**
 * All constraints must reduce to the following normal form for tiles:
 * - OR ( (tile1 XOR tile2 XOR ...) AND (tile3 XOR tile4 XOR ...) AND ... ... )
 *
 * This models each point in the grid as satisfying at least one set of properties that consist of
 * each of a number of planes having a particular tile at that point.
 */
sealed interface PointTileConstraint: TileAssignmentPredicate {

    /**
     * Given a set of active tiles at a point, returns true if the constraint is satisfied.
     *
     * This is not useful for compiling to the LP formulation, but can be used to debug at a higher
     * level, e.g. with a pre-solver. This is also useful for forming visualizations.
     */
    infix fun is_satisfied_for(tiles: Set<Tile>): Boolean
}

class IsOneOf(val tiles: Set<Tile>) : PointTileConstraint {
    constructor(vararg tiles: Tile) : this(tiles.toSet())

    override fun is_satisfied_for(tiles: Set<Tile>): Boolean {
        return tiles.any { it in this.tiles }
    }

}

class IsNoneOf(val tiles: Set<Tile>) : PointTileConstraint {
    constructor(vararg tiles: Tile) : this(tiles.toSet())

    override fun is_satisfied_for(tiles: Set<Tile>): Boolean {
        return tiles.none { it in this.tiles }
    }
}

class Xor(val constraints: Set<PointTileConstraint>) : PointTileConstraint {
    constructor(vararg constraints: PointTileConstraint) : this(constraints.toSet())

    override fun is_satisfied_for(tiles: Set<Tile>): Boolean {
        return constraints.count { it.is_satisfied_for(tiles) } == 1
    }
}

class Or(val constraints: Set<PointTileConstraint>) : PointTileConstraint {
    constructor(vararg constraints: PointTileConstraint) : this(constraints.toSet())

    override fun is_satisfied_for(tiles: Set<Tile>): Boolean {
        return constraints.any { it.is_satisfied_for(tiles) }
    }
}

class And(val constraints: Set<PointTileConstraint>) : PointTileConstraint {
    constructor(vararg constraints: PointTileConstraint) : this(constraints.toSet())

    override fun is_satisfied_for(tiles: Set<Tile>): Boolean {
        return constraints.all { it.is_satisfied_for(tiles) }
    }
}
