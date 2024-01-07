package grid_model.plane

/**
 * A grid layout problem can be subdivided into separate logical "planes".
 *
 * During problem creation, each plane is will be associated with set of tiles, and may have known
 * restrictions ( such as mutually exclusive, etc.,) postulated in advance based on the Plane type.
 */
sealed class Plane {
    val nice_name: String
        get() = javaClass.simpleName
}

abstract class ExclusivePlane : Plane()

abstract class NonExclusivePlane : Plane()
