package com.example.practicecomposeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.practicecomposeapp.ui.theme.PracticeComposeAppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PracticeComposeAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CounterScreen()
                }
            }
        }
    }
}

@Composable
fun CounterScreen(viewModel: CounterViewModel = viewModel()) {
    val countState by viewModel.state.collectAsState()

    CounterContent(
        countState.count,
        isIncrementButtonEnabled = countState.isIncrementButtonEnabled,
        isDecrementButtonEnabled = countState.isDecrementButtonEnabled,
        onAction = { action ->
            when (action) {
                CounterAction.Increment -> viewModel.increment()
                CounterAction.Decrement -> viewModel.decrement()
                CounterAction.Clear -> viewModel.clear()
            }
        }
    )
}

@Composable
fun CounterContent(
    counter: Int,
    isIncrementButtonEnabled: Boolean,
    isDecrementButtonEnabled: Boolean,
    onAction: (CounterAction) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 現在のカウンター値を表示
        Text(
            text = "Counter Value: $counter",
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = { onAction(CounterAction.Increment) },
            enabled = isIncrementButtonEnabled
        ) {
            Text("Increment")
        }

        Button(
            onClick = { onAction(CounterAction.Decrement) },
            enabled = isDecrementButtonEnabled
        ) {
            Text("Decrement")
        }

        Button(onClick = {
            onAction(CounterAction.Clear)
        }) {
            Text("Clear")
        }
    }
}

class CounterViewModel(initialCount: Int = 0) : ViewModel() {
    val MAX_LIMIT = 10
    val MIN_LIMIT = 0

    private val _state = MutableStateFlow(buildState(count = initialCount))
    val state = _state.asStateFlow()

    private fun applyRulesToCount(count: Int): Int {
        return when {
            count < MIN_LIMIT -> MIN_LIMIT
            count > MAX_LIMIT -> MAX_LIMIT
            else -> count
        }
    }

    private fun canIncrement(count: Int) = count < MAX_LIMIT

    private fun canDecrement(count: Int) = count > MIN_LIMIT

    private fun buildState(count: Int): CounterState {
        val clampCount = applyRulesToCount(count)

        return CounterState(
            count = clampCount,
            isIncrementButtonEnabled = canIncrement(clampCount),
            isDecrementButtonEnabled = canDecrement(clampCount)
        )
    }

    fun increment() {
        _state.update { it ->
            buildState(it.count + 1)
        }
    }

    fun decrement() {
        _state.update { it ->
            buildState(it.count - 1)
        }
    }

    fun clear() {
        _state.update { it -> buildState(0) }
    }
}

data class CounterState(
    val count: Int = 0,
    val isIncrementButtonEnabled: Boolean = true,
    val isDecrementButtonEnabled: Boolean = true
)

sealed class CounterAction() {
    data object Increment : CounterAction()
    data object Decrement : CounterAction()
    data object Clear : CounterAction()
}