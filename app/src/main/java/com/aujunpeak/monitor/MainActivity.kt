package com.aujunpeak.monitor

import android.content.Context
import android.content.Intent
import android.content.ActivityNotFoundException
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.aujunpeak.monitor.auth.KeyGate
import com.aujunpeak.monitor.monitor.SystemSampler
import com.aujunpeak.monitor.monitor.SystemSnapshot
import com.aujunpeak.monitor.monitor.deviceReport
import com.aujunpeak.monitor.ui.Accent
import com.aujunpeak.monitor.ui.MonitorUi
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        val prefs = getSharedPreferences("aujunpeak_user", Context.MODE_PRIVATE)
        val sampler = SystemSampler(applicationContext)

        setContent {
            var unlocked by remember { mutableStateOf(prefs.getBoolean("unlocked", false)) }
            var refreshSeconds by remember { mutableIntStateOf(prefs.getInt("refresh_seconds", 1).coerceIn(1, 3)) }
            var accent by remember {
                mutableStateOf(Accent.entries.firstOrNull { it.name == prefs.getString("accent", "CYAN") } ?: Accent.CYAN)
            }
            var snapshot by remember { mutableStateOf(sampler.sample()) }
            var history by remember { mutableStateOf(listOf(snapshot)) }

            LaunchedEffect(unlocked, refreshSeconds) {
                while (unlocked) {
                    delay(refreshSeconds * 1000L)
                    val next = sampler.sample()
                    snapshot = next
                    history = (history + next).takeLast(60)
                }
            }

            MonitorUi(
                unlocked = unlocked,
                snapshot = snapshot,
                history = history,
                accent = accent,
                refreshSeconds = refreshSeconds,
                onLogin = { entered ->
                    val valid = KeyGate.isValid(entered)
                    if (valid) {
                        prefs.edit().putBoolean("unlocked", true).apply()
                        unlocked = true
                    }
                    valid
                },
                onLogout = {
                    prefs.edit().putBoolean("unlocked", false).apply()
                    unlocked = false
                },
                onRefresh = {
                    val next = sampler.sample()
                    snapshot = next
                    history = (history + next).takeLast(60)
                    Toast.makeText(this, "Đã cập nhật số liệu", Toast.LENGTH_SHORT).show()
                },
                onRefreshSecondsChange = { value ->
                    refreshSeconds = value
                    prefs.edit().putInt("refresh_seconds", value).apply()
                },
                onAccentChange = { selected ->
                    accent = selected
                    prefs.edit().putString("accent", selected.name).apply()
                },
                onOpenSettings = { action -> openSettingSafely(action) },
                onCopy = {
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Aujunpeak System Report", deviceReport(snapshot)))
                    Toast.makeText(this, "Đã sao chép báo cáo", Toast.LENGTH_SHORT).show()
                },
                onShare = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, deviceReport(snapshot))
                    }
                    startActivity(Intent.createChooser(intent, "Chia sẻ báo cáo"))
                }
            )
        }
    }

    private fun openSettingSafely(action: String) {
        try {
            val intent = if (action == Settings.ACTION_APPLICATION_DETAILS_SETTINGS) {
                Intent(action, Uri.parse("package:$packageName"))
            } else Intent(action)
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        } catch (_: SecurityException) {
            Toast.makeText(this, "Thiết bị không hỗ trợ mục cài đặt này", Toast.LENGTH_SHORT).show()
        }
    }
}
