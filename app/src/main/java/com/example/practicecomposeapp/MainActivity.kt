package com.example.practicecomposeapp

import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.practicecomposeapp.ui.theme.PracticeComposeAppTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val context = LocalContext.current

    LaunchedEffect(countState.systemMessage) {
        val message = countState.systemMessage
        if (message != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.messageShown()
        }
    }

    CounterContent(
        countState.count,
        isIncrementButtonEnabled = countState.isIncrementButtonEnabled,
        isDecrementButtonEnabled = countState.isDecrementButtonEnabled,
        isClearPending = countState.isClearPending,
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
    isClearPending: Boolean,
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

        if (isClearPending) {
            Text(
                text = "リセット待機中...",
                color = Color.Red,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

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

        Button(
            onClick = {
                onAction(CounterAction.Clear)
            },
            enabled = !isClearPending
        ) {
            Text("Clear")
        }
    }
}

class CounterViewModel(initialCount: Int = 0) : ViewModel() {
    val MAX_LIMIT = 10
    val MIN_LIMIT = 0

    private val _state = MutableStateFlow(buildState(count = initialCount))
    val state = _state.asStateFlow()

    var clearJob: Job? = null

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
            isDecrementButtonEnabled = canDecrement(clampCount),
            isClearPending = false
        )
    }

    fun increment() {
        clearJob?.cancel()

        _state.update { it ->
            buildState(it.count + 1)
        }
    }

    fun decrement() {
        clearJob?.cancel()

        _state.update { it ->
            buildState(it.count - 1)
        }
    }

    fun clear() {
        clearJob?.cancel()

        _state.update { it ->
            it.copy(isClearPending = true, systemMessage = "3秒後にリセット")
        }

        clearJob = viewModelScope.launch {
            try {
                delay(3000L)

                _state.update { it ->
                    buildState(count = 0)
                }
            } catch (e: Exception) {
                _state.update { it -> it.copy(isClearPending = false) }
            }
        }
    }

    fun messageShown() {
        _state.update { it.copy(systemMessage = null) }
    }
}

data class CounterState(
    val count: Int = 0,
    val isIncrementButtonEnabled: Boolean = true,
    val isDecrementButtonEnabled: Boolean = true,
    val isClearPending: Boolean = false,
    val systemMessage: String? = null
)

sealed class CounterAction() {
    data object Increment : CounterAction()
    data object Decrement : CounterAction()
    data object Clear : CounterAction()
}