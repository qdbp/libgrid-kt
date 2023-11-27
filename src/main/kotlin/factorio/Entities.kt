package factorio

import grid_model.Entity
import grid_model.Extent
import grid_model.GridDimension
import grid_model.Plane
import model.*
import grid_model.planes.OntologicalPlane

// class Belt: Entity {
//     override val name: String = "Belt"
//
//     context(OntologicalPlane, GridDimension)
//     override fun get_extent_map(): Map<Plane<*>, Extent<*>> {
//         return mapOf(
//             OntologicalPlane to PointExtent(this),
//             BeltColorPlane to grid_model.Extent.empty(),
//             BeltInputPlane to grid_model.Extent.empty(),
//             BeltOutputPlane to grid_model.Extent.empty()
//         )
//     }
// }
