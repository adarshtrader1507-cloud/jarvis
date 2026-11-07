package com.jarvis.assistant.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val Cyan = Color(0xFF34E8FF)

@Composable
fun CalculatorScreen(
    expression: String,
    result: String,
    onDigit: (String) -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onEquals: () -> Unit,
) {
    val buttons = listOf(
        "(", ")", "⌫", "÷",
        "7", "8", "9", "×",
        "4", "5", "6", "-",
        "1", "2", "3", "+",
        "0", ".", "C", "=",
    )
    Column(Modifier.fillMaxSize().background(Color.Black).padding(16.dp)) {
        Text("Calculator", color = Cyan, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(24.dp))
        Column(
            Modifier.fillMaxWidth().padding(vertical = 24.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Text(expression.ifEmpty { "0" }, color = Color.White, style = MaterialTheme.typography.headlineSmall)
            Text(result, color = Cyan, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.weight(1f)) {
            items(buttons) { label ->
                OutlinedButton(
                    onClick = {
                        when (label) {
                            "C" -> onClear()
                            "⌫" -> onBackspace()
                            "=" -> onEquals()
                            else -> onDigit(label)
                        }
                    },
                    modifier = Modifier.padding(4.dp).aspectRatio(1f),
                ) { Text(label, color = Cyan) }
            }
        }
    }
}
