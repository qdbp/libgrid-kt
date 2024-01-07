package grid_model

import boolean_algebra.BooleanExpr
import grid_model.geom.Dim
import grid_model.plane.Plane
import grid_model.predicate.LPP
import grid_model.tiles.Tile
import boolean_algebra.BooleanExpr.Companion.and as expr_and
import boolean_algebra.BooleanExpr.Companion.or as expr_or

/**
 * A [Demand] object is a set of local demands imposed on tiles by the existence of an entity.
 *
 * As per dogma 2, the [Demand] has no knowledge of its absolute coordinates in the grid, or the
 * size of the grid. All of its constraints are always relative to the origin of the extent.
 *
 * By itself, an [Demand] is a [Plane]-agnostic. One can think of it as a generalization of a pure
 * [grid_model.Shape] which instead of simple points, has some logical conditions on what tiles are
 * valid and invalid in what arrangements.
 *
 * Thus, [Demand]s may be rendered in any given [Plane]. Implementations will pass this plane to
 * individual predicate constructors, which usually *do* require a concrete plane to be fully
 * specified.
 *
 * Implementers are encouraged to make flexible intermediate Demand classes that can be usefully
 * specialized to many different planes.
 */
abstract class Demand<D : Dim<D>> {

    /** How the satisfaction of this extent's tiles relate to the satisfaction of the entity. */
    val ontology: DemandOntology
        get() = IfAndOnlyIf

    /** Enumerates all the tiles this extent will use. */
    protected abstract fun get_active_tiles(): Set<Tile>

    /** Renders this [Demand] as a [BooleanExpr] over single point grid predicates ([LPP]s). */
    abstract fun expr(): BEGP<D>

    val active_tiles: Set<Tile> by lazy { get_active_tiles() }

    // TODO full flexibility will come from handling now raw demands but BooleanExpr<Demand<D>>s
    //  adding new layer to the general compilation hierarchy
    //  Eventually these ad-hoc methods should be migrated to a more general framework. At the same
    //  time, the need to handle `active_tiles` and ontologies suggest that this more limited form
    //  of demand aggregation might be the best approach... think about it...

    fun and(other: Demand<D>): Demand<D> {
        require(other.ontology == this.ontology)
        return let { self ->
            object : Demand<D>() {
                override fun get_active_tiles(): Set<Tile> =
                    self.get_active_tiles() + other.get_active_tiles()

                override fun expr(): BEGP<D> = expr_and(self.expr(), other.expr())
            }
        }
    }

    fun or(other: Demand<D>): Demand<D> {
        require(other.ontology == this.ontology)
        return let { self ->
            object : Demand<D>() {
                override fun get_active_tiles(): Set<Tile> =
                    self.get_active_tiles() + other.get_active_tiles()

                override fun expr(): BEGP<D> = expr_or(self.expr(), other.expr())
            }
        }
    }
}
