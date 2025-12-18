package com.dibs.binecuetoiso

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class CompatibilityTest {
    @Test
    fun scanStreamForSignatures_detectsSystemCnf() {
        val content = "RANDOMDATA...SYSTEM.CNF...MORE"
        val input = ByteArrayInputStream(content.toByteArray(Charsets.ISO_8859_1))
        val sig = listOf("SYSTEM.CNF".toByteArray(Charsets.ISO_8859_1))
        assertTrue(scanStreamForSignatures(input, sig))
    }

    @Test
    fun scanStreamForSignatures_returnsFalseWhenNotFound() {
        val content = "THIS DOES NOT HAVE IT"
        val input = ByteArrayInputStream(content.toByteArray(Charsets.ISO_8859_1))
        val sig = listOf("SYSTEM.CNF".toByteArray(Charsets.ISO_8859_1))
        assertFalse(scanStreamForSignatures(input, sig))
    }
}
