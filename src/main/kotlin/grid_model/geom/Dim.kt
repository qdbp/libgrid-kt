package grid_model.geom

/**
 * We're continuing the good pattern established with LPDomain here:
 *
 * create a "reified type class" with lots of useful fundamental math, and embed an instance of this
 * deep into the bone marrow of the other domain objects.
 */
@Suppress("UNCHECKED_CAST")
sealed class Dim<D : Dim<D>>(final override val ndim: Int) : BaseDim {
    val vlat
        get() = VecLattice(this as D)

    val blat
        get() = (this as D).run { BBLattice() }

    val shape_monoid
        get() = ShapeMonoid(this as D)
}

data object D1 : Dim<D1>(1)

data object D2 : Dim<D2>(2)

data object D3 : Dim<D3>(3)

data object D4 : Dim<D4>(4)
