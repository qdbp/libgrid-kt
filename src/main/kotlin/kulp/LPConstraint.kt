package kulp

// TODO see TODO on Renderable as well, we need to reform + formalize the "variable tree" concept
//  and traversing/discovering renderables
/**
 * Base interface for constraints in the LP model.
 *
 * A note: constraints do NOT "own" their variables. Their render method will not enumerate their
 * terms. They are considered primitive, and their owning objects are responsible for rendering
 * their terms.
 */
interface LPConstraint : LPRenderable
