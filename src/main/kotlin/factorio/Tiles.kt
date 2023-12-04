package factorio

import grid_model.Tile
import model.SegName

enum class Orientation {
    North,
    East,
    South,
    West,
}

enum class BeltColor : Tile {
    Yellow,
    Red,
    Blue;

    // TODO can we abstract an enum base class that does this automatically, somehow?
    override fun tile_name(): SegName = SegName(this.name)
}

enum class BeltInput(orientation: Orientation) : Tile {
    North(Orientation.North),
    East(Orientation.East),
    South(Orientation.South),
    West(Orientation.West);

    override fun tile_name(): SegName = SegName(this.name)
}

enum class BeltOutput(orientation: Orientation) : Tile {
    North(Orientation.North),
    East(Orientation.East),
    South(Orientation.South),
    West(Orientation.West);

    override fun tile_name(): SegName = SegName(this.name)
}
