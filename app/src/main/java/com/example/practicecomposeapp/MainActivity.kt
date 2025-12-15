package com.example.practicecomposeapp

import android.content.Context
import android.os.Bundle
import android.util.Log
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
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.practicecomposeapp.ui.theme.PracticeComposeAppTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

class MainActivity : ComponentActivity() {
    private lateinit var counterViewModel: CounterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // DB と DAO を取得
        val database = CountLogDatabase.getDatabase(applicationContext)
        val dao = database.countLogDao()

        // Factory から ViewModel を生成
        val factory = CounterViewModelFactory(dao)
        counterViewModel = androidx.lifecycle.ViewModelProvider(this, factory)
            .get(CounterViewModel::class.java)

        setContent {
            PracticeComposeAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CounterScreen(counterViewModel)
                }
            }
        }
    }
}

@Composable
fun CounterScreen(viewModel: CounterViewModel) {
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
                CounterAction.CanselClear -> viewModel.canselClear()
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

            Button(
                onClick = { onAction(CounterAction.CanselClear) }
            ) {
                Text("Cancel")
            }
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

class CounterViewModel(initialCount: Int = 0, private val countLogDao: CountLogDao) : ViewModel() {
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

        addCountLog()
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

    fun canselClear() {
        clearJob?.cancel()
        clearJob = null
        _state.update { it ->
            it.copy(
                isClearPending = false,
                systemMessage = "リセットをキャンセルしました"
            )
        }
    }

    fun messageShown() {
        _state.update { it.copy(systemMessage = null) }
    }

    fun addCountLog() {
        val newLog = CountLog(message = "1カウントしました")

        viewModelScope.launch {
            countLogDao.insertCountLog(newLog)

            // 1回だけ最新のリストを取得してログ出力
            countLogDao.getAllCountLog()
                .take(1) // 最初の1回だけ受け取る
                .collect { list ->
                    Log.d("test", "addCountLog: $list")
                }
        }
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
    data object CanselClear : CounterAction()
}

@Entity(tableName = "count_log_table")
data class CountLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val message: String
)

@Dao
interface CountLogDao {
    @Query("SELECT * FROM count_log_table ORDER BY id ASC")
    fun getAllCountLog(): Flow<List<CountLog>>

    @Insert
    suspend fun insertCountLog(countLog: CountLog)

    @Query("DELETE FROM count_log_table WHERE id = :countLogId")
    suspend fun deleteCountLog(countLogId: Int)
}

@Database(entities = [CountLog::class], version = 1, exportSchema = false)
abstract class CountLogDatabase : RoomDatabase() {

    abstract fun countLogDao(): CountLogDao

    companion object {
        @Volatile
        private var INSTANCE: CountLogDatabase? = null

        fun getDatabase(context: Context): CountLogDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CountLogDatabase::class.java,
                    "countLog_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// CountLogDao を受け取る Factory クラスを定義
class CounterViewModelFactory(private val countLogDao: CountLogDao) :
    androidx.lifecycle.ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CounterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CounterViewModel(countLogDao = countLogDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}