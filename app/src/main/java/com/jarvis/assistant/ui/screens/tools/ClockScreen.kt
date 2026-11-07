package com.jarvis.assistant.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

private val Cyan = Color(0xFF34E8FF)

/** Offline, fully functional digital clock + date (Phase 7) — ticks every second, no network. */
@Composable
fun ClockScreen() {
    val now by produceState(initialValue = Date()) {
        while (true) {
            value = Date()
            delay(1000)
        }
    }
    val timeFormat = remember { SimpleDateFormat("hh:mm:ss a", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()) }

    Column(
        Modifier.fillMaxSize().background(Color.Black).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Clock", color = Cyan, style = MaterialTheme.typography.titleLarge)
        androidx.compose.foundation.layout.Spacer(Modifier.padding(32.dp))
        Text(timeFormat.format(now), color = Color.White, style = MaterialTheme.typography.displayMedium)
        Text(dateFormat.format(now), color = Cyan.copy(alpha = 0.8f), style = MaterialTheme.typography.titleMedium)
    }
}
