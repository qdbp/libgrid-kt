package test_kulp

import kulp.LPNode
import kulp.variables.LPInteger
import kotlin.test.Test
import kotlin.test.assertEquals

private object Vars {
    val root = LPNode.new_root()
    val x = root { "x" { LPInteger(1, 3) } }
    val y = root { "y" { LPInteger(null, -9) } }
    val z = root { "z" { LPInteger(-27, null) } }
}

class TestExpression {

    @Test
    fun test_bounds_basic() {
        var resolved = Vars.x.compute_bounds(Vars.root)
        assertEquals(1, resolved.lb)
        assertEquals(3, resolved.ub)

        resolved = Vars.y.compute_bounds(Vars.root)
        assertEquals(null, resolved.lb)
        assertEquals(-9, resolved.ub)

        resolved = Vars.z.compute_bounds(Vars.root)
        assertEquals(-27, resolved.lb)
        assertEquals(null, resolved.ub)
    }

    @Test
    fun test_bounds_add() {
        val expr = Vars.x + Vars.y
        var resolved = expr.compute_bounds(Vars.root)
        assert(resolved.lb == null)
        assert(resolved.ub == -6)

        val expr2 = Vars.x + Vars.z
        resolved = expr2.compute_bounds(Vars.root)
        assert(resolved.lb == -26)
        assert(resolved.ub == null)

        val expr3 = Vars.y - Vars.z
        resolved = expr3.compute_bounds(Vars.root)
        assert(resolved.lb == null)
        assert(resolved.ub == 18)
    }

    @Test
    fun test_bounds_add_mul() {
        val expr4 = Vars.x * 3 - Vars.y * 2 + Vars.z * 1
        val resolved = expr4.compute_bounds(Vars.root)
        assertEquals(-6, resolved.lb)
        assertEquals(null, resolved.ub)
    }
}
