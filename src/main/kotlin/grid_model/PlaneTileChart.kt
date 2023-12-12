package grid_model

interface PlaneTileChart {
    fun all_planes(): Collection<Plane>

    fun plane_of(tile: Tile): Plane

    // need list here because the order matters and must be fixed
    fun tiles_of(plane: Plane): List<Tile>

    fun all_tiles(): Set<Tile> = all_planes().flatMap { tiles_of(it) }.toSet()
}
