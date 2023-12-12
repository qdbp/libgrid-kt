package ivory.functor

// TODO revisit simulated HKTs with witnesses and figure something out here

// there is, I think, no way to type this properly without HKTs:
// interface Functor<out F : Functor<F, *>, out T> {
//     fun <V> fmap(op: (T) -> V): F<V> ... impossible syntax here
// }

// ad hoc implementation for third party types:
// let us not forget that functions are functors over the return type
fun <T, U, V> ((T) -> (U)).fmap(op: (U) -> V): (T) -> V = { op(this(it)) }
