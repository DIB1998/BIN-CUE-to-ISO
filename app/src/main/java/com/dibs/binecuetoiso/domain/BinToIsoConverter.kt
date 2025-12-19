package com.dibs.binecuetoiso.domain

import android.util.Log
import java.io.DataInputStream
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object BinToIsoConverter {

    private const val TAG = "BinToIsoConverter"
    private const val BYTES_PER_SECTOR_RAW = 2352
    private const val ISO_SECTOR_DATA_SIZE = 2048
    private const val FRAMES_PER_SECOND = 75

    // Regex to capture TRACK number and mode
    private val trackRegex = """^\s*TRACK\s+(\d+)\s+([A-Z0-9/]+)\s*$""".toRegex()
    // Regex to capture INDEX 01 timestamp
    private val indexRegex = """^\s*INDEX\s+01\s+(\d{2}):(\d{2}):(\d{2})\s*$""".toRegex()

    private data class TrackInfo(
        val number: Int,
        val mode: String,
        val index01: Long // Start position in bytes
    )

    private fun parseCueSheet(cueInput: InputStream): List<TrackInfo> {
        val tracks = mutableListOf<TrackInfo>()
        var currentTrackInfo: Pair<Int, String>? = null

        cueInput.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                trackRegex.matchEntire(line)?.let { matchResult ->
                    val (trackNumber, trackMode) = matchResult.destructured
                    currentTrackInfo = trackNumber.toInt() to trackMode
                } ?: indexRegex.matchEntire(line)?.let { matchResult ->
                    currentTrackInfo?.let { (trackNumber, trackMode) ->
                        val (minutes, seconds, frames) = matchResult.destructured.toList().map { it.toLong() }

                        val totalFrames = (minutes * 60 + seconds) * FRAMES_PER_SECOND + frames
                        val startIndexInBytes = totalFrames * BYTES_PER_SECTOR_RAW

                        tracks.add(TrackInfo(trackNumber, trackMode, startIndexInBytes))
                        currentTrackInfo = null // Reset after adding track
                    }
                }
            }
        }

        if (tracks.isEmpty()) {
            throw CueFileParseException("Nenhuma trilha válida encontrada no arquivo CUE.")
        }

        return tracks
    }

    fun convert(
        binInput: InputStream,
        cueInput: InputStream,
        isoOutput: OutputStream,
        binSize: Long,
        onProgress: (Float) -> Unit
    ) {
        val tracks = parseCueSheet(cueInput)

        val dataTrack = tracks.firstOrNull { it.mode.startsWith("MODE1") || it.mode.startsWith("MODE2") }
            ?: throw UnsupportedTrackModeException("Nenhuma trilha de dados (MODE1 ou MODE2) encontrada no arquivo CUE.")

        val (sectorSize, dataOffset) = when (dataTrack.mode.uppercase()) {
            "MODE1/2352" -> BYTES_PER_SECTOR_RAW to 16
            "MODE2/2352" -> BYTES_PER_SECTOR_RAW to 24
            "MODE1/2048" -> ISO_SECTOR_DATA_SIZE to 0
            else -> throw UnsupportedTrackModeException("Modo de trilha não suportado: ${dataTrack.mode}")
        }

        val dataTrackIndex = tracks.indexOf(dataTrack)
        val nextTrack = tracks.getOrNull(dataTrackIndex + 1)
        val endPosition = nextTrack?.index01 ?: binSize
        
        val trackSize = endPosition - dataTrack.index01

        var bytesToSkip = dataTrack.index01
        while (bytesToSkip > 0) {
            val skipped = binInput.skip(bytesToSkip)
            if (skipped <= 0) {
                throw IOException("Não foi possível pular para o início da trilha de dados. Fim do arquivo atingido prematuramente.")
            }
            bytesToSkip -= skipped
        }

        val dataIn = DataInputStream(binInput)
        val buffer = ByteArray(sectorSize)
        var totalRead = 0L

        while (totalRead < trackSize) {
            val remaining = trackSize - totalRead
            if (remaining < sectorSize) {
                // Not enough data for a full sector, stop reading.
                break
            }

            try {
                dataIn.readFully(buffer)
            } catch (e: EOFException) {
                // Reached end of file, which can be expected if the last sector is incomplete.
                Log.w(TAG, "EOFException reached, conversion may have ended slightly early.", e)
                break
            }

            isoOutput.write(buffer, dataOffset, ISO_SECTOR_DATA_SIZE)

            totalRead += sectorSize
            val progress = (totalRead.toFloat() / trackSize).coerceAtMost(1.0f)
            onProgress(progress)
        }
    }
}