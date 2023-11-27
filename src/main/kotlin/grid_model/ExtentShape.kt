package grid_model

import mdspan.ndindex

/** Base class for modelling the abstract geometrical shape that an extent can take. */
context(GridDimension)
interface ExtentShape {

    fun get_points(): List<List<Int>>

    fun shifted(shift: List<Int>): ExtentShape {
        return object : ExtentShape {
            override fun get_points(): List<List<Int>> {
                val out = mutableListOf<List<Int>>()
                for (point in get_points()) {
                    out.add(point.zip(shift).map { it.first + it.second })
                }
                return out
            }
        }
    }

    operator fun plus(other: ExtentShape): ExtentShape {
        val points = (get_points() + other.get_points()).distinct()
        return object : ExtentShape {
            override fun get_points(): List<List<Int>> {
                return points
            }
        }
    }
}

fun List<List<Int>>.as_shape(): ExtentShape =
    object : ExtentShape {
        override fun get_points(): List<List<Int>> {
            return this@as_shape
        }
    }

context(GridDimension)
class Rectangle(val size: List<Int>) : ExtentShape {
    override fun get_points(): List<List<Int>> {
        require(size.size == ndim)
        return ndindex(size)
    }
}
