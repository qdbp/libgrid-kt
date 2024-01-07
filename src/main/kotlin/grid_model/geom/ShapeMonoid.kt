package grid_model.geom

import ivory.algebra.Monoid

class ShapeMonoid<D : Dim<D>>(val dim: D) : Monoid<Shape<D>> {

    override val id: Shape<D> = empty(dim)

    override infix fun Shape<D>.op(other: Shape<D>): Shape<D> = this + other
}
