package kulp

/**
 * Any object that has a representation as an affine expression.
 *
 * TODO somehow implement this for Number??
 */
interface LPExprLike {
    fun as_expr(): LPAffineExpression
}
