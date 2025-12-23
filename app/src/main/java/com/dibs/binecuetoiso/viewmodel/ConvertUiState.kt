package com.dibs.binecuetoiso.viewmodel

import android.net.Uri

sealed class ConversionResult {
    object Idle : ConversionResult()
    object Success : ConversionResult()
    data class Error(val message: String) : ConversionResult()
}

sealed class DialogState {
    object Hidden : DialogState()
    data class InvalidFile(val title: String, val message: String) : DialogState()
}

data class ConvertUiState(
    val binUri: Uri? = null,
    val cueUri: Uri? = null,
    val isoOutputName: String = "",
    val progress: Float = 0f,
    val isConverting: Boolean = false,
    val conversionResult: ConversionResult = ConversionResult.Idle,
    val dialogState: DialogState = DialogState.Hidden
)
