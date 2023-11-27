package mdspan

sealed class SliceLike
object ALL : SliceLike()
class IDX(val index: Int) : SliceLike()
class SLC(val start: Int, val end: Int) : SliceLike()
class SEL(val indices: List<Int>) : SliceLike()
