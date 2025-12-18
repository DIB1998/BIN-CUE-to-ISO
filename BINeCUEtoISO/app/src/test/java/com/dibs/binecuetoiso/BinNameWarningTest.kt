package com.dibs.binecuetoiso

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BinNameWarningTest {
    @Test
    fun warning_when_names_differ() {
        val warn = buildBinMismatchWarning("selected.bin", "referenced.bin")
        assertEquals("Atenção: CUE referencia 'referenced.bin' mas BIN selecionado é 'selected.bin'. Prosseguindo.\n", warn)
    }

    @Test
    fun no_warning_when_names_equal_ignoring_case() {
        val warn = buildBinMismatchWarning("GAME.BIN", "game.bin")
        assertNull(warn)
    }

    @Test
    fun no_warning_when_referenced_null() {
        val warn = buildBinMismatchWarning("selected.bin", null)
        assertNull(warn)
    }
}
