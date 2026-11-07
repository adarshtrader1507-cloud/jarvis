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
import com.jarvis.assistant.viewmodel.RemindersViewModel
import java.text.SimpleDateFormat
import java.util.*

private val Cyan = Color(0xFF34E8FF)

@Composable
fun RemindersScreen(viewModel: RemindersViewModel) {
    val reminders by viewModel.reminders.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var label by remember { mutableStateOf("") }
    var minutesFromNow by remember { mutableStateOf("5") }
    val formatter = remember { SimpleDateFormat("d MMM, hh:mm a", Locale.getDefault()) }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text("Reminders", color = Cyan, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            LazyColumn {
                items(reminders, key = { it.id }) { reminder ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1420)),
                    ) {
                        Row(
                            Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(reminder.label, color = Color.White)
                                Text(
                                    formatter.format(Date(reminder.triggerAtMillis)),
                                    color = Cyan.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                            IconButton(onClick = { viewModel.cancel(reminder) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Cancel", tint = Color(0xFFFF4040))
                            }
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { label = ""; minutesFromNow = "5"; showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = Cyan,
        ) { Icon(Icons.Filled.Add, contentDescription = "Add reminder") }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("New reminder") },
            text = {
                Column {
                    OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Remind me to…") })
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = minutesFromNow,
                        onValueChange = { minutesFromNow = it.filter { c -> c.isDigit() } },
                        label = { Text("Minutes from now") },
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val mins = minutesFromNow.toLongOrNull() ?: 5L
                    if (label.isNotBlank()) {
                        viewModel.create(label, System.currentTimeMillis() + mins * 60_000L)
                    }
                    showDialog = false
                }) { Text("Set") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } },
        )
    }
}
