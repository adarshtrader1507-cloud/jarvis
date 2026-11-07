package com.jarvis.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jarvis.assistant.viewmodel.MemoryViewModel

private val Cyan = Color(0xFF34E8FF)

@Composable
fun MemoryScreen(viewModel: MemoryViewModel) {
    val memories by viewModel.memories.collectAsState()

    Column(Modifier.fillMaxSize().background(Color.Black).padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Long-term Memory", color = Cyan, style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = { viewModel.forgetAll() }) { Text("Clear all", color = Color(0xFFFF4040)) }
        }
        if (memories.isEmpty()) {
            Text(
                "Nothing saved yet. Jarvis only remembers things you explicitly approve.",
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 24.dp),
            )
        }
        LazyColumn {
            items(memories, key = { it.id }) { memory ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1420)),
                ) {
                    Row(
                        Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(memory.key, color = Cyan)
                            Text(memory.value, color = Color.White)
                        }
                        IconButton(onClick = { viewModel.forget(memory) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Forget", tint = Color(0xFFFF4040))
                        }
                    }
                }
            }
        }
    }
}
