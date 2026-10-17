package com.aujunpeak.monitor.monitor

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.Process
import android.os.StatFs
import android.os.SystemClock
import java.util.Locale
import kotlin.math.max

data class SystemSnapshot(
    val elapsedMs: Long,
    val cpuAppPercent: Float,
    val ramPercent: Float,
    val ramUsedBytes: Long,
    val ramTotalBytes: Long,
    val storagePercent: Float,
    val storageUsedBytes: Long,
    val storageTotalBytes: Long,
    val batteryPercent: Int,
    val charging: Boolean,
    val batteryTempC: Float?,
    val uptimeMs: Long,
)

/** Only public Android APIs. CPU is app-process CPU, NOT device-wide CPU. */
class SystemSampler(context: Context) {
    private val appContext = context.applicationContext
    private val activityManager = appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private var lastCpuMs = Process.getElapsedCpuTime()
    private var lastTimeMs = SystemClock.elapsedRealtime()
    private val cpuCores = max(1, Runtime.getRuntime().availableProcessors())

    fun sample(): SystemSnapshot {
        val now = SystemClock.elapsedRealtime()
        val cpuNow = Process.getElapsedCpuTime()
        val elapsedWall = now - lastTimeMs
        val elapsedCpu = cpuNow - lastCpuMs
        // Normalized to all logical cores: 100% means all accessible cores saturated by this app.
        val cpu = if (elapsedWall > 0L) {
            (elapsedCpu.toFloat() / (elapsedWall * cpuCores) * 100f).coerceIn(0f, 100f)
        } else 0f
        lastTimeMs = now
        lastCpuMs = cpuNow

        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val ramTotal = memoryInfo.totalMem.coerceAtLeast(1L)
        val ramUsed = (ramTotal - memoryInfo.availMem).coerceIn(0L, ramTotal)

        val stat = StatFs(Environment.getDataDirectory().absolutePath)
        val storageTotal = stat.totalBytes.coerceAtLeast(1L)
        val storageUsed = (storageTotal - stat.availableBytes).coerceIn(0L, storageTotal)

        val batteryManager = appContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryIntent = appContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val reportedPct = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val fallbackPct = if (level >= 0 && scale > 0) (100 * level / scale) else 0
        val batteryPct = if (reportedPct in 0..100) reportedPct else fallbackPct
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
        val rawTemp = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        val temp = if (rawTemp >= 0) rawTemp / 10f else null

        return SystemSnapshot(
            elapsedMs = now,
            cpuAppPercent = cpu,
            ramPercent = ramUsed * 100f / ramTotal,
            ramUsedBytes = ramUsed,
            ramTotalBytes = ramTotal,
            storagePercent = storageUsed * 100f / storageTotal,
            storageUsedBytes = storageUsed,
            storageTotalBytes = storageTotal,
            batteryPercent = batteryPct.coerceIn(0, 100),
            charging = charging,
            batteryTempC = temp,
            uptimeMs = SystemClock.elapsedRealtime(),
        )
    }
}

fun readableSize(bytes: Long): String = String.format(Locale.US, "%.1f GB", bytes.toDouble() / 1_073_741_824.0)

fun readableUptime(ms: Long): String {
    val min = ms / 60_000L
    return "${min / 60}h ${min % 60}m"
}

fun deviceReport(snapshot: SystemSnapshot): String = buildString {
    appendLine("AUJUNPEAK MONITOR · BÁO CÁO THIẾT BỊ")
    appendLine("Thiết bị: ${Build.MANUFACTURER} ${Build.MODEL}")
    appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
    appendLine("CPU ứng dụng: ${"%.1f".format(Locale.US, snapshot.cpuAppPercent)}% (quy đổi toàn bộ lõi)")
    appendLine("RAM thiết bị: ${"%.1f".format(Locale.US, snapshot.ramPercent)}% · ${readableSize(snapshot.ramUsedBytes)} / ${readableSize(snapshot.ramTotalBytes)}")
    appendLine("Lưu trữ phân vùng dữ liệu: ${"%.1f".format(Locale.US, snapshot.storagePercent)}% · ${readableSize(snapshot.storageUsedBytes)} / ${readableSize(snapshot.storageTotalBytes)}")
    appendLine("Pin: ${snapshot.batteryPercent}%${if (snapshot.charging) " · đang sạc" else ""}")
    appendLine("Nhiệt độ pin: ${snapshot.batteryTempC?.let { "%.1f °C".format(Locale.US, it) } ?: "Không có dữ liệu"}")
    appendLine("Uptime: ${readableUptime(snapshot.uptimeMs)}")
    appendLine("Lưu ý: Android không cho mọi ứng dụng đọc CPU toàn thiết bị một cách ổn định.")
}
