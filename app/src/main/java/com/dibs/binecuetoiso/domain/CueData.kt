package com.dibs.binecuetoiso.domain

internal const val BYTES_PER_SECTOR_RAW = 2352
internal const val ISO_SECTOR_DATA_SIZE = 2048
private const val MODE1_DATA_OFFSET = 16
private const val MODE2_DATA_OFFSET = 24

internal sealed class TrackMode(val sectorSize: Int, val dataOffset: Int) {
    object Mode12352 : TrackMode(BYTES_PER_SECTOR_RAW, MODE1_DATA_OFFSET)
    object Mode22352 : TrackMode(BYTES_PER_SECTOR_RAW, MODE2_DATA_OFFSET)
    object Mode12048 : TrackMode(ISO_SECTOR_DATA_SIZE, 0)
    data class Unsupported(val mode: String) : TrackMode(0, 0)

    companion object {
        fun fromString(mode: String): TrackMode {
            return when (mode.uppercase()) {
                "MODE1/2352" -> Mode12352
                "MODE2/2352" -> Mode22352
                "MODE1/2048" -> Mode12048
                else -> Unsupported(mode)
            }
        }
    }
}

internal data class TrackInfo(
    val number: Int,
    val mode: String,
    val index01: Long // Start position in bytes
)
