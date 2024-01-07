package grid_model

import boolean_algebra.BooleanExpr.Companion.and
import boolean_algebra.BooleanExpr.Companion.or
import grid_model.extents.*
import grid_model.geom.Dim
import grid_model.geom.Shape
import grid_model.geom.zeros
import grid_model.plane.Plane
import grid_model.tiles.BosonTile
import grid_model.tiles.Tile
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


fun tile(): TileDelegateFactory = TileDelegateFactory()

class TileDelegateFactory {
    operator fun provideDelegate(thisRef: Any?, prop: KProperty<*>) = ReadOnlyProperty<Any?, Tile> { _, _ -> BosonTile(prop.name) }
}


fun <D: Dim<D>> entity(dim: D, op: context(NewEntCtx<D>) () -> Unit): EntityDelegateFactory<D> =
    EntityDelegateFactory(dim, op)


class EntityDelegateFactory<D: Dim<D>>(val dim: D, val op: context(NewEntCtx<D>) () -> Unit) {
    operator fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, Entity<D>> {
        val ent = NewEntCtx(dim, prop.name)(op)
        return ReadOnlyProperty { _, _ -> ent }
    }
}

/** User-friendly entrypoint into new entity construction using a "DSL-style builder" pattern. */
fun <D : Dim<D>> entity(dim: D, name: String, op: context(NewEntCtx<D>) () -> Unit): Entity<D> =
    NewEntCtx(dim, name)(op)

@DslMarker
private annotation class GridDSLMarker

@GridDSLMarker
class NewEntCtx<D : Dim<D>>(val dim: D, val name: String) {

    val plane_demand_map = mutableMapOf<Plane, TileDemand<D>>()

    private var neighbor_demand: HasAnyOfNeighbors<D>? = null

    operator fun Plane.invoke(op: context(NewTileDmdCtx<D>) () -> Unit) {
        plane_demand_map[this@Plane] =
            NewTileDmdCtx<D>(this@Plane, this@NewEntCtx, dim).apply(op).build()
    }

    fun one_of(vararg neighbors: Entity<D>, op: context(NewOneOfNeighborsContext<D>) () -> Unit) {
        this@NewEntCtx.neighbor_demand =
            NewOneOfNeighborsContext(neighbors.toSet(), this@NewEntCtx, dim).apply(op).build()
    }

    operator fun invoke(op: context(NewEntCtx<D>) () -> Unit): Entity<D> {
        op(this)
        return object : Entity<D> {
            override val name = this@NewEntCtx.name

            override fun active_planes(): Collection<Plane> = plane_demand_map.keys

            override fun <P : Plane> tile_demands_for(plane: P): TileDemand<D>? = plane_demand_map[plane]

            override fun neighbors_demand(): HasAnyOfNeighbors<D>? = neighbor_demand

            override fun toString(): String = "ent_$name"

            override fun hashCode(): Int = (Entity<*>::javaClass to name).hashCode()

            override fun equals(other: Any?): Boolean = other is Entity<*> && other.name == name
        }
    }
}

@GridDSLMarker
abstract class NewDmdCtx<D: Dim<D>, Dem: Demand<D>>(val entity_ctx: NewEntCtx<D>, val dim: D) {

    // default shape is a single-tile extent
    var shape: Shape<D> = Shape(setOf(dim.zeros()))
    operator fun Shape<D>.unaryPlus() { shape = this }

    protected var ontology: DemandOntology = IfAndOnlyIf
    // names are not intuitive and should probably stay that way to discourage use
    fun onto_taut() { ontology = IfAndOnlyIf }
    fun onto_push() { ontology = EntImpliesExt }
    fun onto_pull() { ontology = ExtImpliesEnt }

    abstract fun build(): Dem
}

class NewOneOfNeighborsContext<D: Dim<D>>(val neighbors: Set<Entity<D>>, entity_ctx: NewEntCtx<D>, dim: D): NewDmdCtx<D, HasAnyOfNeighbors<D>>(entity_ctx, dim) {

    override fun build(): HasAnyOfNeighbors<D> = object : HasAnyOfNeighbors<D>() {
        override val neighbors: Set<Entity<D>> = this@NewOneOfNeighborsContext.neighbors
        override val shape: Shape<D> = this@NewOneOfNeighborsContext.shape
    }
}

class NewTileDmdCtx<D : Dim<D>>(private val plane: Plane, entity_ctx: NewEntCtx<D>, dim: D): NewDmdCtx<D, TileDemand<D>>(entity_ctx, dim) {

    private sealed class DemandType
    private class TypeFermi(val name: String) : DemandType()
    private class TypeBoson(val name: String) : DemandType()

    private fun default_tile_name() = "${entity_ctx.name}_${plane.nice_name}"

    // default is to prohibit masked tiles
    protected var allow_masked: Boolean = false
    fun allow_masked(it: Boolean = true) { allow_masked = it }

    // default is to require all the tiles in shape, which is the intuitive behavior
    protected var relator: GPReducer<D> = ::and
    fun all_of() { relator = ::and }
    fun need_one() { relator = ::or }


    // default is fermi, a simple "physical object"
    private var which: DemandType = TypeFermi(default_tile_name())
    fun boson() { which = TypeBoson(default_tile_name()) }
    fun boson(name: String) { which = TypeBoson(name) }
    fun fermi() { which = TypeFermi(default_tile_name()) }
    fun fermi(name: String) { which = TypeFermi(name) }


    override fun build(): TileDemand<D> {
        return when (val it = which) {
            is TypeFermi ->  object: FermiTileDemand<D>() {
                override val relator: GPReducer<D> = this@NewTileDmdCtx.relator
                override val shape: Shape<D> = this@NewTileDmdCtx.shape
                override val tile_name: String = it.name
                override val allow_masked: Boolean = this@NewTileDmdCtx.allow_masked
            }
            is TypeBoson -> object : BosonTileDemand<D>() {
                override val relator: GPReducer<D> = this@NewTileDmdCtx.relator
                override val shape: Shape<D> = this@NewTileDmdCtx.shape
                override val tile_name: String = it.name
                override val allow_masked: Boolean = this@NewTileDmdCtx.allow_masked
            }
        }
    }
}
