package com.jarvis.assistant.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jarvis.assistant.di.AppContainer
import com.jarvis.assistant.di.ViewModelFactory
import com.jarvis.assistant.ui.screens.*
import com.jarvis.assistant.ui.screens.tools.CalculatorScreen
import com.jarvis.assistant.ui.screens.tools.ClockScreen
import com.jarvis.assistant.ui.screens.tools.NotesScreen
import com.jarvis.assistant.ui.screens.tools.RemindersScreen
import com.jarvis.assistant.viewmodel.*

object Routes {
    const val HOME = "home"
    const val TOOLS = "tools"
    const val CALCULATOR = "calculator"
    const val CLOCK = "clock"
    const val NOTES = "notes"
    const val REMINDERS = "reminders"
    const val MEMORY = "memory"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
}

/**
 * Phase 7 wiring: every Smart Tool, Settings, Memory and History screen is a real,
 * reachable destination — nothing here is a decorative button that goes nowhere.
 */
@Composable
fun JarvisNavGraph(
    container: AppContainer,
    onMicClick: () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    val factory = ViewModelFactory(container)
    val jarvisViewModel: JarvisViewModel = viewModel(factory = factory)

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = jarvisViewModel,
                onMicClick = onMicClick,
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                onToolsClick = { navController.navigate(Routes.TOOLS) },
            )
        }
        composable(Routes.TOOLS) {
            WithBack(navController) { ToolsScreen(onNavigate = { route -> navController.navigate(route) }) }
        }
        composable(Routes.CALCULATOR) {
            val vm: CalculatorViewModel = viewModel(factory = factory)
            WithBack(navController) {
                CalculatorScreen(
                    expression = vm.expression.value,
                    result = vm.result.value,
                    onDigit = vm::append,
                    onClear = vm::clear,
                    onBackspace = vm::backspace,
                    onEquals = vm::equals,
                )
            }
        }
        composable(Routes.CLOCK) { WithBack(navController) { ClockScreen() } }
        composable(Routes.NOTES) {
            val vm: NotesViewModel = viewModel(factory = factory)
            WithBack(navController) { NotesScreen(viewModel = vm) }
        }
        composable(Routes.REMINDERS) {
            val vm: RemindersViewModel = viewModel(factory = factory)
            WithBack(navController) { RemindersScreen(viewModel = vm) }
        }
        composable(Routes.MEMORY) {
            val vm: MemoryViewModel = viewModel(factory = factory)
            WithBack(navController) { MemoryScreen(viewModel = vm) }
        }
        composable(Routes.HISTORY) {
            val messages by jarvisViewModel.messages.collectAsState()
            val memoryViewModel: MemoryViewModel = viewModel(factory = factory)
            val prefs by jarvisViewModel.prefs.collectAsState()
            WithBack(navController) {
                ChatHistoryScreen(
                    messages = messages,
                    onClear = { jarvisViewModel.clearConversation() },
                    onSaveToMemory = { key, value ->
                        if (prefs.memoryEnabled) memoryViewModel.remember(key, value)
                    },
                )
            }
        }
        composable(Routes.SETTINGS) {
            val vm: SettingsViewModel = viewModel(factory = factory)
            WithBack(navController) { SettingsScreen(viewModel = vm) }
        }
    }
}

/** Lightweight back-button overlay so every pushed screen is actually navigable back
 *  from — no dead-end destinations. */
@Composable
private fun WithBack(navController: NavHostController, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        content()
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF34E8FF))
        }
    }
}

