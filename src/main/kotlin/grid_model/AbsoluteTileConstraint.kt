package grid_model

// import grid_model.PointTilePredicate

/**
 * Absolutely-located tile constraints are the bridge class between the abstract formulation of
 * tile constraints and the concrete language of the grid model.
 */
data class AbsoluteTileConstraint(
    val position: List<Int>,
    val constraint: Any // PointTilePredicate,
) {
    // TODO : LPRenderable

}

/**
 * This class has an identical representation to SpatialConstraint, but is distinct to avoid
 * confusion between the two.
 */
// FIXME
data class LocalTileConstraint(val rel_coords: List<Int>, val constraint: Any) {
    fun anchor(position: List<Int>): AbsoluteTileConstraint {
        return AbsoluteTileConstraint(
            position = position.zip(rel_coords).map { (a, b) -> a + b },
            constraint = constraint
        )
    }

    fun shifted(offset: List<Int>): LocalTileConstraint {
        require(offset.size == rel_coords.size)
        return LocalTileConstraint(
            rel_coords = offset.zip(rel_coords).map { (a, b) -> a + b },
            constraint = constraint
        )
    }
}
