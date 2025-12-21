package com.dibs.binecuetoiso

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dibs.binecuetoiso.ui.ConvertScreen
import com.dibs.binecuetoiso.ui.theme.BINeCUEtoISOTheme

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BINeCUEtoISOTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { viewModel.onShowLanguageDialog() }) {
                    Icon(
                        Icons.Default.Language,
                        contentDescription = stringResource(R.string.change_language)
                    )
                }
            }
            ConvertScreen(modifier = Modifier)
        }
    }

    if (uiState.showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = uiState.currentLanguage,
            onDismissRequest = { viewModel.onDismissLanguageDialog() },
            onLanguageSelected = { viewModel.setLocale(it) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionDialog(
    currentLanguage: String,
    onDismissRequest: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    val languages = listOf("en" to stringResource(R.string.language_english), "pt" to stringResource(R.string.language_portuguese))
    var selectedLanguage by rememberSaveable { mutableStateOf(currentLanguage) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.choose_language)) },
        text = {
            Column {
                languages.forEach { (code, name) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (selectedLanguage == code),
                                onClick = { selectedLanguage = code }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedLanguage == code),
                            onClick = { selectedLanguage = code }
                        )
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onLanguageSelected(selectedLanguage) }
            ) {
                Text(stringResource(R.string.dialog_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}
