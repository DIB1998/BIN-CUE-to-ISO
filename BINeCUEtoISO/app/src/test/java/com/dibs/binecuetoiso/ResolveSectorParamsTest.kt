package com.dibs.binecuetoiso

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResolveSectorParamsTest {
    @Test
    fun resolves_known_modes() {
        assertEquals(Triple(2352, 16, 2048), resolveSectorParams("MODE1/2352"))
        assertEquals(Triple(2352, 24, 2048), resolveSectorParams("MODE2/2352"))
        assertEquals(Triple(2048, 0, 2048), resolveSectorParams("MODE1/2048"))
    }

    @Test
    fun returns_null_for_unknown_mode() {
        assertNull(resolveSectorParams(null))
        assertNull(resolveSectorParams("SOMETHING/UNKNOWN"))
    }
}
