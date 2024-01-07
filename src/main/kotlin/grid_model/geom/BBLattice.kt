package grid_model.geom

import ivory.order.Lattice
import ivory.order.PartialOrder.Companion.pgeq
import ivory.order.PartialOrder.Companion.pleq
import ivory.order.Rel

/** null is empty b-box */
context(D)
class BBLattice<D : Dim<D>> : Lattice<BBox<D>?> {

    override infix fun (BBox<D>?).cmp(other: BBox<D>?): Rel? {
        if (this == null) return Rel.LEQ
        if (other == null) return Rel.GEQ
        return vlat.run {
            when {
                lower pleq other.lower && upper pgeq other.upper -> Rel.GEQ
                lower pgeq other.lower && upper pleq other.upper -> Rel.LEQ
                else -> null
            }
        }
    }

    override fun (BBox<D>?).meet(other: BBox<D>?): BBox<D>? {
        if (this == null) return null
        if (other == null) return null
        return vlat.run {
            if (
                (lower pgeq other.upper && lower != other.upper) ||
                    (upper pleq other.lower && upper != other.lower)
            ) {
                BBox(lower join other.lower, upper meet other.upper)
            } else {
                null
            }
        }
    }

    /** The join is the smallest BBox containing both. */
    override infix fun (BBox<D>?).join(other: BBox<D>?): BBox<D>? {
        if (this == null) return other
        if (other == null) return this
        return vlat.run { BBox(lower meet other.lower, upper join other.upper) }
    }
}
