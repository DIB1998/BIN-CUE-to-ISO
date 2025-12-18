package com.dibs.binecuetoiso

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class ConvertStreamTest {
    @Test
    fun convertAndCopyStream_extractsDataFromSectors() {
        val sectorSize = 2352
        val dataOffset = 16
        val dataSize = 2048
        val sectors = 2

        val inputBytes = ByteArray(sectorSize * sectors)
        for (s in 0 until sectors) {
            val fillByte: Byte = s.toByte()
            for (i in 0 until dataSize) {
                inputBytes[s * sectorSize + dataOffset + i] = fillByte
            }
        }

        val input = ByteArrayInputStream(inputBytes)
        val output = ByteArrayOutputStream()

        val limitBytes = (sectorSize * sectors).toLong()

        convertAndCopyStream(input, output, sectorSize, dataOffset, dataSize, limitBytes)

        val out = output.toByteArray()
        assertEquals(dataSize * sectors, out.size)

        // first sector bytes should be 0
        for (i in 0 until dataSize) {
            assertEquals(0.toByte(), out[i])
        }
        // second sector bytes should be 1
        for (i in 0 until dataSize) {
            assertEquals(1.toByte(), out[dataSize + i])
        }
    }

    @Test
    fun convertAndCopyStream_stopsOnPartialSector() {
        val sectorSize = 2352
        val dataOffset = 16
        val dataSize = 2048

        // only 1 full sector + a partial second sector
        val sectorsFull = 1
        val partialBytes = 100

        val inputBytes = ByteArray(sectorSize * sectorsFull + partialBytes)
        for (s in 0 until sectorsFull) {
            val fillByte: Byte = s.toByte()
            for (i in 0 until dataSize) {
                inputBytes[s * sectorSize + dataOffset + i] = fillByte
            }
        }

        val input = ByteArrayInputStream(inputBytes)
        val output = ByteArrayOutputStream()

        val limitBytes = (sectorSize * (sectorsFull + 1)).toLong()

        convertAndCopyStream(input, output, sectorSize, dataOffset, dataSize, limitBytes)

        val out = output.toByteArray()
        // only one full sector should be written
        assertEquals(dataSize * sectorsFull, out.size)
    }

    @Test
    fun scanStreamForSignatures_detectsAndRejectsCorrectly() {
        val hay = "randomdata\nSYSTEM.CNF\nmoredata".toByteArray(Charsets.ISO_8859_1)
        val input = ByteArrayInputStream(hay)

        val signatures = listOf("SYSTEM.CNF".toByteArray(Charsets.ISO_8859_1))

        assertTrue(scanStreamForSignatures(input, signatures))
    }

    @Test
    fun scanStreamForSignatures_negativeCase() {
        val hay = "no relevant strings here".toByteArray(Charsets.ISO_8859_1)
        val input = ByteArrayInputStream(hay)

        val signatures = listOf("SYSTEM.CNF".toByteArray(Charsets.ISO_8859_1))

        assertTrue(!scanStreamForSignatures(input, signatures))
    }
}
