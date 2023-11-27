package grid_model

sealed class GridDimension(val ndim: Int) {
    abstract fun origin(): List<Int>
}

object D1 : GridDimension(1) {
    override fun origin(): List<Int> = listOf(0)}
object D2 : GridDimension(2) {
    override fun origin(): List<Int> = listOf(0, 0) }
object D3 : GridDimension(3) {
    override fun origin(): List<Int> = listOf(0, 0, 0)}
