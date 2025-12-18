package com.dibs.binecuetoiso

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream

class DetectModeTest {
    @Test
    fun detectMode_mode1_2048() {
        // place 'CD001' at sector 16, offset 1 in a 2048 sector layout
        val sectorSize = 2048
        val dataOffset = 0
        val sectorIndex = 16
        val pos = sectorIndex * sectorSize + dataOffset + 1
        val buf = ByteArray(pos + 10)
        val sig = "CD001".toByteArray(Charsets.ISO_8859_1)
        System.arraycopy(sig, 0, buf, pos, sig.size)

        val detected = detectTrackModeFromStream(ByteArrayInputStream(buf))
        assertEquals("MODE1/2048", detected)
    }

    @Test
    fun detectMode_mode1_2352() {
        // place 'CD001' at sector 16, inside a 2352 sector with 16-byte offset
        val sectorSize = 2352
        val dataOffset = 16
        val sectorIndex = 16
        val pos = sectorIndex * sectorSize + dataOffset + 1
        val buf = ByteArray(pos + 10)
        val sig = "CD001".toByteArray(Charsets.ISO_8859_1)
        System.arraycopy(sig, 0, buf, pos, sig.size)

        val detected = detectTrackModeFromStream(ByteArrayInputStream(buf))
        assertEquals("MODE1/2352", detected)
    }

    @Test
    fun detectMode_negative_noSignature() {
        val buf = "no signatures here".toByteArray(Charsets.ISO_8859_1)
        val detected = detectTrackModeFromStream(ByteArrayInputStream(buf))
        assertEquals(null, detected)
    }
}
