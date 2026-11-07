package com.jarvis.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private val Cyan = Color(0xFF34E8FF)

private data class Tool(val label: String, val icon: ImageVector, val route: String)

@Composable
fun ToolsScreen(onNavigate: (String) -> Unit) {
    val tools = listOf(
        Tool("Calculator", Icons.Filled.Calculate, "calculator"),
        Tool("Clock", Icons.Filled.AccessTime, "clock"),
        Tool("Notes", Icons.Filled.Notes, "notes"),
        Tool("Reminders", Icons.Filled.Alarm, "reminders"),
        Tool("Memory", Icons.Filled.Psychology, "memory"),
        Tool("History", Icons.Filled.History, "history"),
    )
    Column(Modifier.fillMaxSize().background(Color.Black).padding(16.dp)) {
        Text("Smart Tools", color = Cyan, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(2)) {
            items(tools) { tool ->
                Card(
                    onClick = { onNavigate(tool.route) },
                    modifier = Modifier.padding(8.dp).aspectRatio(1.2f).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1420)),
                ) {
                    Column(
                        Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    ) {
                        Icon(tool.icon, contentDescription = tool.label, tint = Cyan, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(tool.label, color = Color.White)
                    }
                }
            }
        }
    }
}
