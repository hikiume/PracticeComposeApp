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
fun CounterScreen() {
    val viewModel: CounterViewModel = viewModel()
    val counter by viewModel.count.collectAsState()

    CounterContent(counter, onIncrementClick = {
        // カウンターの値をインクリメント
        viewModel.increment()
    })
}

@Composable
fun CounterContent(
    counter: Int,
    onIncrementClick: () -> Unit
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

        // ボタンがクリックされた時の処理（ロジック）もComposable関数内にある
        Button(onClick = onIncrementClick) {
            Text("Increment")
        }
    }
}

class CounterViewModel : ViewModel() {
    private val _count = MutableStateFlow(0)
    val count = _count.asStateFlow()

    fun increment() {
        _count.update { it -> it + 1 }
    }
}