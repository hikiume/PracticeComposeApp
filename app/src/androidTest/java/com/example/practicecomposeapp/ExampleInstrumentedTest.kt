package com.example.practicecomposeapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun before() {
    }

    @Test
    // 初期値が0であることを確認する
    fun initial_state_shows_zero_count() {
        setComposeTestRuleContext()

        composeTestRule
            .onNodeWithText("Counter Value: 0")
            .assertIsDisplayed()
    }

    @Test
    // Incrementボタンを押すと1加算されることを確認
    fun increment_button_increases_counter_value() {
        setComposeTestRuleContext()

        composeTestRule
            .onNodeWithText("Increment")
            .performClick()

        composeTestRule
            .onNodeWithText("Counter Value: 1")
            .assertIsDisplayed()
    }

    @Test
    // カウンターが上限に達したとき、Incrementボタンが無効化されるべき
    fun incrementButton_shouldBeDisabled_whenCounterReachesMaxLimit() {
        setComposeTestRuleContext(9)

        composeTestRule
            .onNodeWithText("Increment")
            .performClick()

        // 現在のカウンター値が10であることを確認
        composeTestRule
            .onNodeWithText("Counter Value: 10")
            .assertExists()

        // ボタンのenabledプロパティがfalseであることを確認
        composeTestRule
            .onNodeWithText("Increment")
            .assertIsNotEnabled()
    }


    @Test
    // Decrementボタンを押すと1減算されることを確認
    fun decrement_button_decrements_counter_value() {
        setComposeTestRuleContext(10)

        composeTestRule
            .onNodeWithText("Decrement")
            .performClick()

        composeTestRule
            .onNodeWithText("Counter Value: 9")
    }

    @Test
    // Decrementボタン_無効になっていること_下限に到達する
    fun decrementButton_shouldBeDisabled_whenCounterReachesMinLimit() {
        setComposeTestRuleContext(1)

        composeTestRule
            .onNodeWithText("Decrement")
            .performClick()

        composeTestRule
            .onNodeWithText("Decrement")
            .assertIsNotEnabled()
    }

    @Test
    // Decrementボタン_有効になっていること_下限から加算された時
    fun decrementButton_shouldBeEnabled_whenCounterIsIncrementedFromMinLimit() {
        setComposeTestRuleContext(0)

        composeTestRule
            .onNodeWithText("Decrement")
            .assertIsNotEnabled()

        composeTestRule
            .onNodeWithText("Increment")
            .performClick()

        composeTestRule
            .onNodeWithText("Decrement")
            .assertIsEnabled()
    }

    @Test
    fun clearButton_showsResetMessage_andDisappearsAfterDelay() {
        setComposeTestRuleContext(10)

        composeTestRule
            .onNodeWithText("Clear")
            .performClick()

        // 「リセット待機中...」が表示されていることを確認
        composeTestRule
            .onNodeWithText("リセット待機中...")
            .assertIsDisplayed()

        sleep(3000L)

        // メッセージが消えていることを確認
        composeTestRule
            .onNodeWithText("リセット待機中...")
            .assertDoesNotExist()
    }

    @Test
    // Clearボタンを押すと0になることを確認
    fun clear_button_resets_counter_to_zero() {
        setComposeTestRuleContext(10)

        composeTestRule
            .onNodeWithText("Clear")
            .performClick()

        composeTestRule.onNodeWithText("Counter Value: 0")
    }

    @Test
    // Cancelボタンを押すとClear状態がキャンセルされる
    fun cancelClearButton() {
        setComposeTestRuleContext(10)

        composeTestRule
            .onNodeWithText("Clear")
            .performClick()

        composeTestRule
            .onNodeWithText("Cancel")
            .assertExists()

        composeTestRule
            .onNodeWithText("Cancel")
            .performClick()

        composeTestRule
            .onNodeWithText("Cancel")
            .assertIsNotDisplayed()

        composeTestRule.onNodeWithText("Counter Value: 10")
    }

    private fun setComposeTestRuleContext(initialValue: Int = 0) {
        // ViewModelをインスタンス化
        val initialMaxViewModel = CounterViewModel(initialCount = initialValue)

        // テスト対象のComposableにこのViewModelを注入
        composeTestRule.setContent {
            CounterScreen(viewModel = initialMaxViewModel)
        }
    }
}