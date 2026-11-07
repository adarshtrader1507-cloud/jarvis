package com.jarvis.assistant.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jarvis.assistant.data.local.entity.NoteEntity
import com.jarvis.assistant.viewmodel.NotesViewModel

private val Cyan = Color(0xFF34E8FF)

@Composable
fun NotesScreen(viewModel: NotesViewModel) {
    val notes by viewModel.notes.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text("Notes", color = Cyan, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            LazyColumn {
                items(notes, key = { it.id }) { note ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1420)),
                    ) {
                        Row(
                            Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(note.title, color = Color.White, style = MaterialTheme.typography.titleMedium)
                                Text(note.content, color = Color.White.copy(alpha = 0.7f))
                            }
                            IconButton(onClick = { viewModel.delete(note) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFFF4040))
                            }
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { title = ""; content = ""; showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = Cyan,
        ) { Icon(Icons.Filled.Add, contentDescription = "Add note") }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("New note") },
            text = {
                Column {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Content") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isNotBlank() || content.isNotBlank()) viewModel.save(title, content)
                    showDialog = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } },
        )
    }
}
