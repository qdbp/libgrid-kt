package kulp.variables

import model.SegName

class LPNonnegativeInteger(name: SegName) : LPInteger(name, 0.bound, LPInfinite)
