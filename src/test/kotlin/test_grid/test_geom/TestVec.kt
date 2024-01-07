package test_grid.test_geom

import grid_model.geom.D3
import grid_model.geom.vec
import kotlin.test.Test
import kotlin.test.assertEquals

class TestVec {

    @Test
    fun test_symmetrize_quadrants() {
        D3.run {
            val foo = vec(1, 2, 3)
            assertEquals(
                setOf(
                    vec(1, 2, 3),
                    vec(1, 2, -3),
                    vec(1, -2, 3),
                    vec(1, -2, -3),
                    vec(-1, 2, 3),
                    vec(-1, 2, -3),
                    vec(-1, -2, 3),
                    vec(-1, -2, -3)
                ),
                foo.symmetrize_quadrants()
            )
        }
    }
}
