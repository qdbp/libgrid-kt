package grid_model

/**
 * A grid layout problem can be subdivided into separate logical "planes".
 *
 * Each plane is defined as having an associated set of *mutually exclusive* tiles. For each plane,
 * each grid cell must be assigned exactly one tile of the associated types.
 *
 * The tiles of multiple planes may overlap.
 *
 * e.g. in Factorio, these Planes might be:
 * - physical extent (tiles = {occupied})
 * - electric coverage (tiles = {supply, demand})
 *
 * Note that there are two classes of planes: universal and private.
 *
 * Physical extent is an example of a universal plane. All entities must resolve against each other.
 * Electrical *supply* is an example of a universal plane as well. However, electrical *demand* is
 * an example of a private plane. Each entity has its own electrical demand.
 */
abstract class Plane(protected val id: PlaneIdentifier) {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is Plane) return false
        return when (id) {
            is Universal -> other.id is Universal
            is Unique -> id === other.id
            is Named -> other.id is Named && other.id.name == id.name
        }
    }

    val nice_name: String =
        when (id) {
            is Universal -> "${this::class.simpleName}"
            is Unique -> "${this::class.simpleName}@${this.id.hashCode()}"
            is Named -> "${this::class.simpleName}@${this.id.name}"
        }

    override fun hashCode(): Int = Pair(this::class, id).hashCode()
}

sealed class PlaneIdentifier

internal object Universal : PlaneIdentifier()

internal class Unique : PlaneIdentifier() {
    override fun equals(other: Any?): Boolean {
        return this === other
    }

    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }
}

internal data class Named(val name: String) : PlaneIdentifier()

/**
 * A universal plane is one that is shared by all entities.
 *
 * e.g. physical extent
 */
abstract class UniversalPlane : Plane(Universal)

/**
 * A unique plane exists in a "private universe" and does not overlap with any other plane, even
 * those with the same tile set.
 *
 * e.g. electrical demand
 */
abstract class UniquePlane : Plane(Unique())

/**
 * A named plane is one that is considered to overlap when it has the same name.
 *
 * e.g. underground belts
 */
abstract class NamedPlane(val name: String) : Plane(Named(name))
