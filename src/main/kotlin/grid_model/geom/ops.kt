package grid_model.geom

import requiring
import kotlin.reflect.KClass

inline fun <reified D : Dim<D>> KClass<D>.fix(): D {
    return when (this) {
        D1::class -> D1
        D2::class -> D2
        D3::class -> D3
        D4::class -> D4
        else -> error("unsupported dimension $this")
    }
        as D
}

fun <D : Dim<D>> D.vec(coords: List<Int>): Vec<D> {
    return Vec(coords requiring { it.size == ndim }, this)
}

fun <D : Dim<D>> D.vec(vararg coords: Int): Vec<D> = vec(coords.toList())

fun <D : Dim<D>> D.zeros(): Vec<D> = vec(List(ndim) { 0 })

fun <D : Dim<D>> D.ones(): Vec<D> = vec(List(ndim) { 1 })

fun <D : Dim<D>> D.ones(fill: Int) = vec(List(ndim) { fill })

fun Pair<Int, Int>.to_vec(): Vec<D2> = D2.vec(first, second)

fun Triple<Int, Int, Int>.to_vec(): Vec<D3> = D3.vec(first, second, third)
fun <D : Dim<D>> empty(dim: D): Shape<D> = Shape(setOf(), dim)