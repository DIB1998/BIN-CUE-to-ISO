package com.dibs.binecuetoiso.domain

import java.io.InputStream

internal object CueParser {

    private const val FRAMES_PER_SECOND = 75

    private val trackRegex = """^\s*TRACK\s+(\d+)\s+([A-Z0-9/]+)\s*$""".toRegex(RegexOption.IGNORE_CASE)
    private val indexRegex = """^\s*INDEX\s+01\s+(\d{2}):(\d{2}):(\d{2})\s*$""".toRegex(RegexOption.IGNORE_CASE)

    fun parse(cueInput: InputStream): List<TrackInfo> {
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
                        currentTrackInfo = null
                    }
                }
            }
        }

        if (tracks.isEmpty()) {
            throw CueFileParseException("Nenhuma trilha válida encontrada no arquivo CUE.")
        }

        return tracks
    }
}
