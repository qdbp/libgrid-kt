package grid_model

import boolean_algebra.BooleanAlgebra
import grid_model.predicate.SPGP

// TODO lift to interface?
/**
 * An extent is a set of local constraints imposed on tiles by the existence of an entity.
 *
 * As per dogma 2, the Extent has no knowledge of its absolute coordinates in the grid, or the size
 * of the grid. All of its constraints are always relative to the origin of the extent.
 */
abstract class Extent<out P : Plane> {

    /** Enumerates all the tiles this extent will use. */
    protected abstract fun get_active_tiles(): List<Tile>

    val active_tiles: List<Tile> by lazy { get_active_tiles() }

    /** Enumerates the predicates that must be true */
    protected abstract fun local_demands(): BooleanAlgebra<SPGP>

    val demands: BooleanAlgebra<SPGP> by lazy { local_demands() }

    override fun equals(other: Any?): Boolean {
        return (other is Extent<*>) && (other.get_active_tiles() == get_active_tiles())
    }

    override fun hashCode(): Int {
        return Pair(this::class, get_active_tiles()).hashCode()
    }
}
