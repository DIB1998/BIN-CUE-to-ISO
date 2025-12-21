package com.dibs.binecuetoiso.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dibs.binecuetoiso.R
import com.dibs.binecuetoiso.viewmodel.ConversionResult
import com.dibs.binecuetoiso.viewmodel.ConvertViewModel
import com.dibs.binecuetoiso.viewmodel.DialogState

@Composable
fun ConvertScreen(
    modifier: Modifier = Modifier,
    convertViewModel: ConvertViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by convertViewModel.uiState.collectAsState()

    val folderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri -> convertViewModel.onFolderSelected(context, uri) }
    )

    val createIsoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
        onResult = { uri: Uri? ->
            if (uri != null) {
                convertViewModel.startConversion(context, uri)
            }
        }
    )

    val dialogState = uiState.dialogState
    if (dialogState is DialogState.InvalidFile) {
        ResultDialog(
            onDismissRequest = { convertViewModel.dismissDialog() },
            title = dialogState.title,
            message = dialogState.message,
            icon = { Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp)) }
        )
    }

    Scaffold(modifier = modifier) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.convert_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { folderLauncher.launch(null) },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.select_folder), style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = if (uiState.binUri != null && uiState.cueUri != null) stringResource(R.string.files_selected) else stringResource(R.string.no_folder_selected),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (uiState.binUri != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = uiState.binUri != null && uiState.cueUri != null) {
                Column {
                    FileSelectedInfo(stringResource(R.string.bin_file), uiState.binUri?.let { DocumentFile.fromSingleUri(context, it)?.name })
                    Spacer(modifier = Modifier.height(8.dp))
                    FileSelectedInfo(stringResource(R.string.cue_file), uiState.cueUri?.let { DocumentFile.fromSingleUri(context, it)?.name })
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                AnimatedVisibility(visible = uiState.isConverting) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { uiState.progress },
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${(uiState.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { createIsoLauncher.launch(uiState.isoOutputName) },
                    enabled = uiState.binUri != null && uiState.cueUri != null && !uiState.isConverting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(stringResource(R.string.convert_to_iso))
                }
            }

            when (val result = uiState.conversionResult) {
                ConversionResult.Success -> {
                    ResultDialog(
                        onDismissRequest = { convertViewModel.resetConversionResult() },
                        title = stringResource(R.string.dialog_success_title),
                        message = stringResource(R.string.dialog_success_message),
                        icon = { Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp)) }
                    )
                }
                is ConversionResult.Error -> {
                    ResultDialog(
                        onDismissRequest = { convertViewModel.resetConversionResult() },
                        title = stringResource(R.string.dialog_error_title),
                        message = result.message,
                        icon = { Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp)) }
                    )
                }
                ConversionResult.Idle -> {}
            }
        }
    }
}

@Composable
fun FileSelectedInfo(title: String, fileName: String?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(text = title, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = fileName ?: "",
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ResultDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    icon: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = icon,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismissRequest) { Text(stringResource(R.string.dialog_ok)) }
        }
    )
}
