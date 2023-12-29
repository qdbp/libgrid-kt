package ivory.functor.etree

/** Expression Tree. */
sealed class ETree<out T, out V> {
    fun <U> fmap(f: (V) -> U): ETree<T, U> =
        when (this) {
            is Atom -> Atom(f(atom))
            is Node -> Node(terms.map { it.fmap(f) })
        }
}

// structureless value
class Atom<V>(val atom: V) : ETree<Nothing, V>()

open class Node<out T, out V>(val terms: List<ETree<T, V>>) : ETree<T, V>()
