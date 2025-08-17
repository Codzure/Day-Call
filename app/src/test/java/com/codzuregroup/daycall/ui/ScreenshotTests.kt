package com.codzuregroup.daycall.ui

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.codzuregroup.daycall.ui.alarm.AddAlarmScreen
import com.codzuregroup.daycall.ui.todo.AddTaskScreenMinimal
import com.codzuregroup.daycall.ui.todo.ModernTodoScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class ScreenshotTests {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun captureHome() {
        composeRule.onRoot().captureToImage()
            .asAndroidBitmap()
            .also { saveBitmap(it, "home") }
    }

    private fun saveBitmap(bitmap: Bitmap, name: String) {
        val dir = File("/sdcard/DayCallScreenshots")
        dir.mkdirs()
        val file = File(dir, "$name.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }

    @Test
    fun addAlarmScreen() {
        composeRule.setContent {
            AddAlarmScreen(
                onBackPressed = {},
                onSaveAlarm = {},
                onTestSound = {},
                audioManager = null
            )
        }
        composeRule.onRoot().captureToImage().asAndroidBitmap()
            .also { saveBitmap(it, "add_alarm_screen") }
    }

    @Test
    fun addTaskScreenMinimal() {
        composeRule.setContent {
            AddTaskScreenMinimal(onBack = {}, onSave = {}, editing = null)
        }
        composeRule.onRoot().captureToImage().asAndroidBitmap()
            .also { saveBitmap(it, "add_task_screen_minimal") }
    }

    @Test
    fun modernTodoScreen() {
        composeRule.setContent {
            ModernTodoScreen(
                onBackPressed = {},
                onNavigateToAddTodo = {},
                onNavigateToEditTodo = {},
                onNavigateToCompleted = {}
            )
        }
        composeRule.onRoot().captureToImage().asAndroidBitmap()
            .also { saveBitmap(it, "modern_todo_screen") }
    }

    // Add similar tests for every other Composable screen. Let me know if you want all auto-generated!
}