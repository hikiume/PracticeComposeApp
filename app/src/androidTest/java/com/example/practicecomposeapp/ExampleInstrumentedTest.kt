package com.example.practicecomposeapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
    fun before(){
        // 画面上のどのコンポーザブルをテストするか定義
        composeTestRule.setContent {
            CounterScreen()
        }
    }

    @Test
    // 初期値が0であることを確認する
    fun initial_state_shows_zero_count() {
        composeTestRule
            .onNodeWithText("Counter Value: 0")
            .assertIsDisplayed()
    }

    @Test
    // Incrementボタンを押すと1加算されることを確認
    fun increment_button_increases_counter_value(){
        composeTestRule
            .onNodeWithText("Increment")
            .performClick()

        composeTestRule
            .onNodeWithText("Counter Value: 1")
            .assertIsDisplayed()
    }

    @Test
    // カウンターが上限に達したとき、Incrementボタンが無効化されるべき
    fun incrementButton_shouldBeDisabled_whenCounterReachesMaxLimit(){
        for(i in 1..10){
            composeTestRule
                .onNodeWithText("Increment")
                .performClick()
        }

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
    fun decrement_button_decrements_counter_value(){

        for(i in 1..10){
            composeTestRule
                .onNodeWithText("Increment")
                .performClick()
        }

        composeTestRule
            .onNodeWithText("Decrement")
            .performClick()

        composeTestRule
            .onNodeWithText("Counter Value: 9")
    }

    @Test
    // Clearボタンを押すと0になることを確認
    fun clear_button_resets_counter_to_zero(){
        composeTestRule
            .onNodeWithText("Increment")
            .performClick()

        composeTestRule
            .onNodeWithText("Clear")
            .performClick()

        composeTestRule.onNodeWithText("Counter Value: 0")
    }
}