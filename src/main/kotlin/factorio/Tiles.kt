package factorio

import grid_model.Tile

enum class Orientation {
    North,
    East,
    South,
    West,
}

enum class BeltColor: Tile {
    Yellow,
    Red,
    Blue,
}

enum class BeltInput(orientation: Orientation): Tile {
    North(Orientation.North),
    East(Orientation.East),
    South(Orientation.South),
    West(Orientation.West),
}

enum class BeltOutput(orientation: Orientation): Tile {
    North(Orientation.North),
    East(Orientation.East),
    South(Orientation.South),
    West(Orientation.West),
}
