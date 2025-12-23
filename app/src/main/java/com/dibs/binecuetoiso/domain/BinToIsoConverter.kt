package com.dibs.binecuetoiso.domain

import android.util.Log
import java.io.DataInputStream
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object BinToIsoConverter {

    private const val TAG = "BinToIsoConverter"

    fun convert(
        binInput: InputStream,
        cueInput: InputStream,
        isoOutput: OutputStream,
        binSize: Long,
        onProgress: (Float) -> Unit
    ) {
        val tracks = CueParser.parse(cueInput)

        val dataTrack = tracks.firstOrNull { it.mode.uppercase().startsWith("MODE1") || it.mode.uppercase().startsWith("MODE2") }
            ?: throw UnsupportedTrackModeException("Nenhuma trilha de dados (MODE1 ou MODE2) encontrada no arquivo CUE.")

        val trackMode = TrackMode.fromString(dataTrack.mode)
        if (trackMode is TrackMode.Unsupported) {
            throw UnsupportedTrackModeException("Modo de trilha não suportado: ${trackMode.mode}")
        }

        val dataTrackIndex = tracks.indexOf(dataTrack)
        val nextTrack = tracks.getOrNull(dataTrackIndex + 1)
        val endPosition = nextTrack?.index01 ?: binSize

        val trackSize = endPosition - dataTrack.index01
        if (trackSize <= 0) {
            throw IOException("Tamanho de trilha inválido. Verifique o arquivo CUE ou a integridade do arquivo BIN.")
        }

        var bytesToSkip = dataTrack.index01
        while (bytesToSkip > 0) {
            val skipped = binInput.skip(bytesToSkip)
            if (skipped <= 0) {
                throw IOException("Não foi possível pular para o início da trilha de dados. Fim do arquivo atingido prematuramente.")
            }
            bytesToSkip -= skipped
        }

        val dataIn = DataInputStream(binInput)
        val buffer = ByteArray(trackMode.sectorSize)
        var totalRead = 0L

        while (totalRead < trackSize) {
            val remaining = trackSize - totalRead
            if (remaining < trackMode.sectorSize) {
                break
            }

            try {
                dataIn.readFully(buffer)
            } catch (e: EOFException) {
                Log.w(TAG, "EOFException reached, conversion may have ended slightly early.", e)
                break
            }

            isoOutput.write(buffer, trackMode.dataOffset, ISO_SECTOR_DATA_SIZE)

            totalRead += trackMode.sectorSize
            val progress = (totalRead.toFloat() / trackSize).coerceAtMost(1.0f)
            onProgress(progress)
        }
    }
}