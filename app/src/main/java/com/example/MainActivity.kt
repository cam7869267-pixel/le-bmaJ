package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.data.service.JamWakeWordService
import com.example.ui.JumblePApp
import com.example.ui.viewmodel.JumblePViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: JumblePViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        if (audioGranted && viewModel.isWakeWordListeningEnabled.value) {
            viewModel.restartWakeWordEngine()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestRequiredPermissions()
        handleWakeWordIntent(intent)

        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                JumblePApp(
                    viewModel = viewModel,
                    onRequestPermissions = { requestRequiredPermissions() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleWakeWordIntent(intent)
    }

    private fun handleWakeWordIntent(intent: Intent?) {
        if (intent == null) return
        val isWakeTriggered = intent.getBooleanExtra(JamWakeWordService.EXTRA_TRIGGER_JAM, false) ||
                intent.action == JamWakeWordService.ACTION_WAKE_WORD_TRIGGERED

        if (isWakeTriggered) {
            val keyword = intent.getStringExtra(JamWakeWordService.EXTRA_WAKE_WORD) ?: "Jam"
            viewModel.triggerWakeWordGreeting(keyword)
        }
    }

    private fun requestRequiredPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}
