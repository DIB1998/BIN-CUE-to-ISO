package com.dibs.binecuetoiso.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dibs.binecuetoiso.domain.BinToIsoConverter
import com.dibs.binecuetoiso.domain.CueFileParseException
import com.dibs.binecuetoiso.domain.UnsupportedTrackModeException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed class ConversionResult {
    object Idle : ConversionResult()
    object Success : ConversionResult()
    data class Error(val message: String) : ConversionResult()
}

class ConvertViewModel : ViewModel() {

    var progress by mutableFloatStateOf(0f)
        private set

    var isConverting by mutableStateOf(false)
        private set

    var conversionResult by mutableStateOf<ConversionResult>(ConversionResult.Idle)
        private set

    fun startConversion(context: Context, binUri: Uri, cueUri: Uri, isoUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            isConverting = true
            progress = 0f
            conversionResult = ConversionResult.Idle

            try {
                val resolver = context.contentResolver
                val binSize = resolver.openFileDescriptor(binUri, "r")?.use { it.statSize } ?: 0L

                val binStream = resolver.openInputStream(binUri) ?: throw Exception("Não foi possível abrir o arquivo BIN.")
                val cueStream = resolver.openInputStream(cueUri) ?: throw Exception("Não foi possível abrir o arquivo CUE.")
                val isoStream = resolver.openOutputStream(isoUri) ?: throw Exception("Não foi possível criar o arquivo ISO.")

                binStream.use { bin ->
                    cueStream.use { cue ->
                        isoStream.use { iso ->
                            BinToIsoConverter.convert(
                                binInput = bin,
                                cueInput = cue,
                                isoOutput = iso,
                                binSize = binSize
                            ) {
                                progress = it
                            }
                        }
                    }
                }
                conversionResult = ConversionResult.Success
            } catch (e: CueFileParseException) {
                conversionResult = ConversionResult.Error(e.message ?: "Erro ao analisar o arquivo CUE.")
            } catch (e: UnsupportedTrackModeException) {
                conversionResult = ConversionResult.Error(e.message ?: "Modo de trilha não suportado.")
            } catch (e: Exception) {
                conversionResult = ConversionResult.Error(e.message ?: "Ocorreu um erro inesperado.")
                e.printStackTrace()
            } finally {
                isConverting = false
            }
        }
    }

    fun resetConversionResult() {
        conversionResult = ConversionResult.Idle
    }
}
