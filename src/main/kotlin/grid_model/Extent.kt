package grid_model

import grid_model.kulp_adapters.PointTilePredicate

/**
 * An extent is a set of local constraints imposed on tiles by the existence of an entity.
 *
 * As per dogma 2, the Extent has no knowledge of its absolute coordinates in the grid, or the
 * size of the grid. All of its constraints are always relative to the origin of the extent.
 */
context(grid_model.GridDimension)
interface Extent {
    companion object {
        fun <T : Tile> empty(): Extent =
            object : Extent {
                override fun get_demands(): List<LocalTileConstraint> = listOf()
            }
    }

    fun get_demands(): List<LocalTileConstraint>

    fun shifted(shift: List<Int>): Extent {
        val demands = get_demands().map { it.shifted(shift) }
        return object : Extent {
            override fun get_demands(): List<LocalTileConstraint> = demands
        }
    }

}


/**
 * An extent that is defined by setting the extent origin of the extent to a particular tile.
 */
context(grid_model.GridDimension)
abstract class AbstractPointExtent<out T : Tile> : Extent {

    override fun get_demands(): List<LocalTileConstraint> {
        return listOf(LocalTileConstraint(origin(), origin_constraint()))
    }

    abstract fun origin_constraint(): PointTilePredicate

    /**
     * Replicates the origin constraint over points in the given shape.
     */
    fun tiled_over(shape: ExtentShape): Extent {
        return object : Extent {
            override fun get_demands(): List<LocalTileConstraint> {
                return shape.get_points().map { LocalTileConstraint(it, origin_constraint()) }
            }
        }
    }
}
