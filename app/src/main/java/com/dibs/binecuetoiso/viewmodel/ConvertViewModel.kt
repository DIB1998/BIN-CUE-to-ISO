package com.dibs.binecuetoiso.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dibs.binecuetoiso.R
import com.dibs.binecuetoiso.domain.BinToIsoConverter
import com.dibs.binecuetoiso.domain.CueFileParseException
import com.dibs.binecuetoiso.domain.UnsupportedTrackModeException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

class ConvertViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ConvertUiState())
    val uiState = _uiState.asStateFlow()

    fun onFolderSelected(context: Context, uri: Uri?) {
        if (uri == null) return

        val folder = DocumentFile.fromTreeUri(context, uri)
        if (folder?.isDirectory != true) {
            _uiState.update {
                it.copy(dialogState = DialogState.InvalidFile(
                    title = context.getString(R.string.error_invalid_selection_title),
                    message = context.getString(R.string.error_invalid_selection_message)
                ))
            }
            return
        }

        val files = folder.listFiles()
        val foundBin = files.find { it.name?.endsWith(".bin", ignoreCase = true) == true }
        val foundCue = files.find { it.name?.endsWith(".cue", ignoreCase = true) == true }

        if (foundBin != null && foundCue != null) {
            _uiState.update {
                it.copy(
                    binUri = foundBin.uri,
                    cueUri = foundCue.uri,
                    isoOutputName = foundBin.name?.substringBeforeLast('.') + ".iso"
                )
            }
        } else {
            _uiState.update {
                it.copy(dialogState = DialogState.InvalidFile(
                    title = context.getString(R.string.error_files_not_found_title),
                    message = context.getString(R.string.error_files_not_found_message)
                ))
            }
        }
    }

    fun startConversion(context: Context, isoUri: Uri) {
        val currentState = _uiState.value
        val binUri = currentState.binUri ?: return
        val cueUri = currentState.cueUri ?: return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isConverting = true, progress = 0f, conversionResult = ConversionResult.Idle) }

            try {
                val resolver = context.contentResolver
                val binSize = resolver.openFileDescriptor(binUri, "r")?.use { it.statSize } ?: 0L

                val binStream = resolver.openInputStream(binUri) ?: throw Exception("Could not open BIN file.")
                val cueStream = resolver.openInputStream(cueUri) ?: throw Exception("Could not open CUE file.")
                val isoStream = resolver.openOutputStream(isoUri) ?: throw Exception("Could not create ISO file.")

                binStream.use { bin ->
                    cueStream.use { cue ->
                        isoStream.use { iso ->
                            BinToIsoConverter.convert(
                                binInput = bin,
                                cueInput = cue,
                                isoOutput = iso,
                                binSize = binSize
                            ) { conversionProgress ->
                                _uiState.update { it.copy(progress = conversionProgress) }
                            }
                        }
                    }
                }
                _uiState.update { it.copy(conversionResult = ConversionResult.Success) }
            } catch (e: CueFileParseException) {
                _uiState.update { it.copy(conversionResult = ConversionResult.Error(e.message ?: "Error parsing CUE file.")) }
            } catch (e: UnsupportedTrackModeException) {
                _uiState.update { it.copy(conversionResult = ConversionResult.Error(e.message ?: "Unsupported track mode.")) }
            } catch (e: Exception) {
                _uiState.update { it.copy(conversionResult = ConversionResult.Error(e.message ?: "An unexpected error occurred.")) }
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(isConverting = false) }
            }
        }
    }

    fun resetConversionResult() {
        _uiState.update { it.copy(conversionResult = ConversionResult.Idle) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(dialogState = DialogState.Hidden) }
    }
}
