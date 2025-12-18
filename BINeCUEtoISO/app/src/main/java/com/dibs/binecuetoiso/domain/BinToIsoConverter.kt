package com.dibs.binecuetoiso.domain

import java.io.DataInputStream
import java.io.EOFException
import java.io.InputStream
import java.io.OutputStream

object BinToIsoConverter {

    private fun getTrackMode(cueInput: InputStream): String {
        val cueContent = cueInput.bufferedReader().use { it.readText() }
        val trackRegex = """TRACK\s+\d+\s+([A-Z0-9/]+)""".toRegex()
        val matchResult = trackRegex.find(cueContent)
        return matchResult?.groups?.get(1)?.value ?: throw CueFileParseException("Não foi possível encontrar o modo da trilha no arquivo CUE.")
    }

    fun convert(
        binInput: InputStream,
        cueInput: InputStream,
        isoOutput: OutputStream,
        binSize: Long,
        onProgress: (Float) -> Unit
    ) {
        val trackMode = getTrackMode(cueInput)

        val (sectorSize, dataOffset) = when (trackMode.uppercase()) {
            "MODE1/2352" -> 2352 to 16
            "MODE2/2352" -> 2352 to 24
            "MODE1/2048" -> 2048 to 0
            else -> throw UnsupportedTrackModeException("Modo de trilha não suportado: $trackMode")
        }

        val dataIn = DataInputStream(binInput)
        val buffer = ByteArray(sectorSize)
        var totalRead = 0L
        val dataSize = 2048 // Setores ISO sempre têm 2048 bytes de dados

        while (totalRead < binSize) {
            try {
                dataIn.readFully(buffer)
            } catch (e: EOFException) {
                break
            }

            isoOutput.write(buffer, dataOffset, dataSize)

            totalRead += sectorSize
            onProgress(totalRead.toFloat() / binSize)
        }
    }
}
