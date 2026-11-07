package com.jarvis.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.jarvis.assistant.BuildConfig
import com.jarvis.assistant.data.prefs.AppLanguage
import com.jarvis.assistant.viewmodel.SettingsViewModel

private val Cyan = Color(0xFF34E8FF)

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val prefs by viewModel.prefs.collectAsState()
    var backendUrlField by remember(prefs.backendUrl) { mutableStateOf(prefs.backendUrl) }
    var authTokenField by remember(prefs.backendAuthToken) { mutableStateOf(prefs.backendAuthToken) }
    var showClearConfirm by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().background(Color.Black).verticalScroll(rememberScrollState()).padding(16.dp),
    ) {
        Text("Settings", color = Cyan, style = MaterialTheme.typography.titleLarge)

        SectionTitle("AI Provider")
        OutlinedTextField(
            value = backendUrlField,
            onValueChange = { backendUrlField = it },
            label = { Text("Backend URL (blank = default)") },
            placeholder = { Text(BuildConfig.BACKEND_BASE_URL) },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = authTokenField,
            onValueChange = { authTokenField = it },
            label = { Text("Backend auth token (APP_SHARED_SECRET)") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        Button(
            onClick = {
                viewModel.setBackendUrl(backendUrlField)
                viewModel.setBackendAuthToken(authTokenField)
            },
            modifier = Modifier.padding(top = 8.dp),
        ) { Text("Save AI provider config") }

        SectionTitle("Language")
        Row {
            AppLanguage.entries.forEach { lang ->
                FilterChip(
                    selected = prefs.language == lang,
                    onClick = { viewModel.setLanguage(lang) },
                    label = { Text(lang.label) },
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
        }

        SectionTitle("Voice speed: ${"%.1f".format(prefs.speechRate)}x")
        Slider(
            value = prefs.speechRate,
            onValueChange = { viewModel.setSpeechRate(it) },
            valueRange = 0.5f..2.0f,
        )

        SectionTitle("Animation intensity: ${"%.0f".format(prefs.animationIntensity * 100)}%")
        Slider(
            value = prefs.animationIntensity,
            onValueChange = { viewModel.setAnimationIntensity(it) },
            valueRange = 0.3f..1.5f,
        )

        SectionTitle("Online / Offline")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Force offline mode", color = Color.White, modifier = Modifier.weight(1f))
            Switch(checked = prefs.forceOfflineMode, onCheckedChange = { viewModel.setForceOffline(it) })
        }

        SectionTitle("Memory & Privacy")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Enable memory", color = Color.White, modifier = Modifier.weight(1f))
            Switch(checked = prefs.memoryEnabled, onCheckedChange = { viewModel.setMemoryEnabled(it) })
        }
        Button(
            onClick = { showClearConfirm = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4040)),
            modifier = Modifier.padding(top = 8.dp),
        ) { Text("Clear all data") }

        SectionTitle("App info")
        Text("JARVIS — Personal Intelligence System", color = Color.White.copy(alpha = 0.7f))
        Text("Version ${BuildConfig.VERSION_NAME}", color = Color.White.copy(alpha = 0.7f))
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear all data?") },
            text = { Text("This permanently deletes conversation history and saved memories. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearAllData(); showClearConfirm = false }) { Text("Clear") }
            },
            dismissButton = { TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        color = Cyan.copy(alpha = 0.9f),
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
    )
}
