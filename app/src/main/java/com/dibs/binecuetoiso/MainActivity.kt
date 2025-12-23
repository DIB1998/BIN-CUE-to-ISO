package com.dibs.binecuetoiso

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dibs.binecuetoiso.ui.MainScreen
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
