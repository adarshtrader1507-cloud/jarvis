package com.jarvis.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jarvis.assistant.viewmodel.ChatMessage

private val Cyan = Color(0xFF34E8FF)

/**
 * Phase 8: long-term memory is explicitly user-approved — Jarvis never silently
 * remembers anything. The bookmark icon on an AI reply is that approval action.
 */
@Composable
fun ChatHistoryScreen(
    messages: List<ChatMessage>,
    onClear: () -> Unit,
    onSaveToMemory: (key: String, value: String) -> Unit,
) {
    var pendingSave by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().background(Color.Black).padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Conversation history", color = Cyan, style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onClear) { Text("Clear", color = Color(0xFFFF4040)) }
        }
        LazyColumn(modifier = Modifier.padding(top = 12.dp)) {
            items(messages) { message ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (message.isUser) Color(0xFF123047) else Color(0xFF0A1420),
                        ),
                    ) {
                        Text(message.text, color = Color.White, modifier = Modifier.padding(10.dp))
                    }
                    if (!message.isUser) {
                        IconButton(onClick = { pendingSave = message.text }) {
                            Icon(Icons.Filled.BookmarkAdd, contentDescription = "Save to memory", tint = Cyan)
                        }
                    }
                }
            }
        }
    }

    val textToSave = pendingSave
    if (textToSave != null) {
        var label by remember(textToSave) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { pendingSave = null },
            title = { Text("Remember this?") },
            text = {
                Column {
                    Text(textToSave, color = Color.Gray, maxLines = 3)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Label (e.g. \"favorite color\")") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onSaveToMemory(label.ifBlank { "Note" }, textToSave)
                    pendingSave = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { pendingSave = null }) { Text("Cancel") } },
        )
    }
}
