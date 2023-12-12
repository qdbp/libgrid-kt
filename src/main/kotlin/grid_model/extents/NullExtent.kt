package grid_model.extents

import boolean_algebra.BooleanAlgebra
import boolean_algebra.True
import grid_model.Extent
import grid_model.Tile
import grid_model.predicate.SPGP

/** This extent represent having no conditions in a given Plane. */
object NullExtent : Extent<Nothing>() {
    override fun get_active_tiles(): List<Tile> = emptyList()

    override fun local_demands(): BooleanAlgebra<SPGP> = True
}
