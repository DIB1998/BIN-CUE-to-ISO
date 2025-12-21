package com.dibs.binecuetoiso

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

data class MainUiState(
    val showLanguageDialog: Boolean = false,
    val currentLanguage: String = AppCompatDelegate.getApplicationLocales()[0]?.language ?: Locale.getDefault().language
)

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    fun onShowLanguageDialog() {
        _uiState.update { it.copy(showLanguageDialog = true) }
    }

    fun onDismissLanguageDialog() {
        _uiState.update { it.copy(showLanguageDialog = false) }
    }

    fun setLocale(language: String) {
        val appLocale = LocaleListCompat.forLanguageTags(language)
        AppCompatDelegate.setApplicationLocales(appLocale)
        _uiState.update { it.copy(showLanguageDialog = false) }
    }
}
