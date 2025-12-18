package com.dibs.binecuetoiso

import org.junit.Assert.assertEquals
import org.junit.Test

class CueParserTest {
    @Test
    fun parseCue_withModeAndIndexAndFile_returnsCorrectInfo() {
        val cue = """
            FILE "game.bin" BINARY
            TRACK 01 MODE1/2352
            INDEX 01 00:00:00
            TRACK 02 AUDIO
            INDEX 01 02:34:05
        """.trimIndent()

        val result = CueParser.parse(cue)

        assertEquals("MODE1/2352", result.trackMode)
        // 2*60*75 + 34*75 + 5 = 11555
        assertEquals(11555L, result.dataTrackEndSectors)
        assertEquals("game.bin", result.referencedBinFileName)
    }

    @Test
    fun parseCue_noIndex_returnsNullEndSectors() {
        val cue = """
            FILE "game.bin" BINARY
            TRACK 01 MODE1/2048
            TRACK 02 AUDIO
        """.trimIndent()

        val result = CueParser.parse(cue)

        assertEquals("MODE1/2048", result.trackMode)
        assertEquals(null, result.dataTrackEndSectors)
    }

    @Test
    fun parseCue_mode2_and_fileWithSpaces_returnsCorrectInfo() {
        val cue = """
            file "My Game (Disc 1).bin" BINARY
            TRACK 01    MODE2/2352
            TRACK 02 AUDIO
            INDEX 01 02:00:00
        """.trimIndent()

        val result = CueParser.parse(cue)

        assertEquals("MODE2/2352", result.trackMode)
        // 2*60*75 = 9000
        assertEquals(9000L, result.dataTrackEndSectors)
        assertEquals("My Game (Disc 1).bin", result.referencedBinFileName)
    }

    @Test
    fun parseCue_singleQuotedFileName_isParsed() {
        val cue = """
            FILE 'game disc.bin' BINARY
            TRACK 01 MODE1/2352
            INDEX 01 00:00:00
            TRACK 02 AUDIO
            INDEX 01 02:34:05
        """.trimIndent()

        val result = CueParser.parse(cue)

        assertEquals("MODE1/2352", result.trackMode)
        assertEquals(11555L, result.dataTrackEndSectors)
        assertEquals("game disc.bin", result.referencedBinFileName)
    }
}
