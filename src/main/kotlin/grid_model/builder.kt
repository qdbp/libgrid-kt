package grid_model

import grid_model.dimension.Dim
import grid_model.extents.Extent
import grid_model.extents.SimpleBosonicExtent
import grid_model.extents.SimpleFermionicExtent
import grid_model.planes.Plane

/** User-friendly entrypoint into new entity construction using a "DSL-style builder" pattern. */
fun <D : Dim<D>> entity(dim: D, name: String, op: (NewEntityContext<D>).() -> Unit): Entity<D> =
    NewEntityContext(dim, name)(op)

class NewEntityContext<D : Dim<D>>(val dim: D, val name: String) {

    val plane_extent_map = mutableMapOf<Plane, Extent<D>>()

    operator fun Plane.invoke(op: context(NewExtentContext<D>) () -> Extent<D>) {
        with (this@NewEntityContext) {
            val nex_ctx = NewExtentContext(dim)
            plane_extent_map[this@Plane] = op(nex_ctx)
        }
        // val nex_ctx = NewExtentContext(dim, this@NewEntityContext)
        // with(this) { plane_extent_map[this] = op(this@NewEntityContext, nex_ctx) }
    }
    operator fun invoke(op: context(NewEntityContext<D>) () -> Unit): Entity<D> {
        op(this)
        return object : Entity<D> {
            override val name = this@NewEntityContext.name

            override fun active_planes(): Collection<Plane> = plane_extent_map.keys

            override fun <P : Plane> get_extent_within(plane: P): Extent<D>? =
                plane_extent_map[plane]

            override fun toString(): String = "Entity[$name]"

            override fun hashCode(): Int = (Entity<*>::javaClass to name).hashCode()

            override fun equals(other: Any?): Boolean = other is Entity<*> && other.name == name
        }
    }
}

context(NewEntityContext<D>)
class NewExtentContext<D : Dim<D>>(val dim: D) {

    val Shape<D>.fermionic: Extent<D> get() = SimpleFermionicExtent(name, this@Shape)

    val Shape<D>.bosonic: Extent<D> get() = SimpleBosonicExtent(name, this@Shape)
}
