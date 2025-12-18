package com.dibs.binecuetoiso.ui

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dibs.binecuetoiso.viewmodel.ConversionResult
import com.dibs.binecuetoiso.viewmodel.ConvertViewModel

@Composable
fun ConvertScreen(convertViewModel: ConvertViewModel = viewModel()) {
    val context = LocalContext.current
    var binUri by remember { mutableStateOf<Uri?>(null) }
    var cueUri by remember { mutableStateOf<Uri?>(null) }
    var isoUri by remember { mutableStateOf<Uri?>(null) }
    var isoOutputName by remember { mutableStateOf("converted.iso") }

    val binLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        binUri = uri
        uri?.let {
            context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex != -1) {
                    val displayName = cursor.getString(nameIndex)
                    isoOutputName = displayName.substringBeforeLast('.') + ".iso"
                }
            }
        }
    }

    val cueLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        cueUri = uri
    }

    val createIsoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        isoUri = uri
        if (binUri != null && cueUri != null && isoUri != null) {
            convertViewModel.startConversion(context, binUri!!, cueUri!!, isoUri!!)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { binLauncher.launch(arrayOf("*/*")) }) {
            Text("Select BIN file")
        }
        binUri?.let { Text("Selected BIN: ${it.path}") }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { cueLauncher.launch(arrayOf("*/*")) }) {
            Text("Select CUE file")
        }
        cueUri?.let { Text("Selected CUE: ${it.path}") }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                createIsoLauncher.launch(isoOutputName)
            },
            enabled = binUri != null && cueUri != null && !convertViewModel.isConverting
        ) {
            Text("Convert to ISO")
        }

        if (convertViewModel.isConverting) {
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { convertViewModel.progress },
                modifier = Modifier.fillMaxWidth()
            )
        }

        when (val result = convertViewModel.conversionResult) {
            ConversionResult.Success -> {
                AlertDialog(
                    onDismissRequest = { convertViewModel.resetConversionResult() },
                    title = { Text("Sucesso") },
                    text = { Text("A conversão foi concluída com sucesso!") },
                    confirmButton = {
                        TextButton(onClick = { convertViewModel.resetConversionResult() }) {
                            Text("OK")
                        }
                    }
                )
            }
            is ConversionResult.Error -> {
                AlertDialog(
                    onDismissRequest = { convertViewModel.resetConversionResult() },
                    title = { Text("Erro") },
                    text = { Text(result.message) },
                    confirmButton = {
                        TextButton(onClick = { convertViewModel.resetConversionResult() }) {
                            Text("OK")
                        }
                    }
                )
            }
            ConversionResult.Idle -> {
                // Não exibe nada
            }
        }
    }
}
