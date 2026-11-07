package com.jarvis.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jarvis.assistant.domain.model.AiState
import com.jarvis.assistant.domain.model.ConnectivityState
import com.jarvis.assistant.ui.components.AiFace
import com.jarvis.assistant.viewmodel.JarvisViewModel

/**
 * Main holographic screen (Phase 2). Everything on it is wired to real state:
 * - AiFace <- viewModel.aiState (Phase 3's state machine, untouched)
 * - ONLINE/OFFLINE badge <- viewModel.connectivity (Phase 6, real ConnectivityManager + prefs)
 * - mic button -> onMicClick -> MainActivity's SpeechRecognizer -> viewModel.onUserUtterance
 * - send button -> viewModel.onUserUtterance directly from typed text
 * - settings / tools buttons -> real navigation routes, not dead ends
 */
@Composable
fun HomeScreen(
    viewModel: JarvisViewModel,
    onMicClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onToolsClick: () -> Unit,
) {
    val aiState by viewModel.aiState.collectAsState()
    val connectivity by viewModel.connectivity.collectAsState()
    val prefs by viewModel.prefs.collectAsState()
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
    ) {
        // TOP SECTION
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("JARVIS", color = Color(0xFF34E8FF), style = MaterialTheme.typography.titleLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                val statusColor = if (connectivity == ConnectivityState.ONLINE) Color(0xFF34E8FF) else Color.Gray
                Text(
                    text = if (connectivity == ConnectivityState.ONLINE) "ONLINE" else "OFFLINE",
                    color = statusColor,
                    style = MaterialTheme.typography.labelMedium,
                )
                IconButton(onClick = onToolsClick) {
                    Icon(Icons.Filled.Build, contentDescription = "Smart Tools", tint = Color.White)
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White)
                }
            }
        }

        // CENTER SECTION — the holographic face is the visual anchor.
        // animationIntensity (Settings dashboard) scales the face without changing its design.
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AiFace(
                    state = aiState,
                    modifier = Modifier.fillMaxWidth(0.75f * prefs.animationIntensity.coerceIn(0.5f, 1f)),
                )
                Spacer(Modifier.height(12.dp))
                Text(text = statusLabel(aiState), color = Color.White.copy(alpha = 0.8f))
            }
        }

        // BOTTOM SECTION
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask Jarvis…") },
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onMicClick) {
                Icon(Icons.Filled.Mic, contentDescription = "Speak", tint = Color(0xFF34E8FF))
            }
            IconButton(onClick = {
                if (inputText.isNotBlank()) {
                    viewModel.onUserUtterance(inputText)
                    inputText = ""
                }
            }) {
                Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color(0xFF34E8FF))
            }
        }
    }
}

private fun statusLabel(state: AiState): String = when (state) {
    AiState.Idle -> "Idle"
    AiState.Listening -> "Listening…"
    AiState.Thinking -> "Thinking…"
    is AiState.Speaking -> "Speaking…"
    AiState.Activating -> "Activating…"
    is AiState.Error -> "⚠ ${state.message}"
}
