package com.aujunpeak.monitor.ui

import android.os.Build
import android.provider.Settings
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aujunpeak.monitor.monitor.SystemSnapshot
import com.aujunpeak.monitor.monitor.readableSize
import com.aujunpeak.monitor.monitor.readableUptime
import java.util.Locale

private val Navy = Color(0xFF070C17)
private val Surface = Color(0xFF101A2B)
private val SurfaceLight = Color(0xFF15243A)
private val StrokeGrey = Color(0xFF25344A)
private val Muted = Color(0xFF8C9AB2)
private val PrimaryText = Color(0xFFEFF7FF)
private val Mint = Color(0xFF41E6B2)
private val Amber = Color(0xFFFFC56D)

enum class Accent(val displayName: String, val tone: Color) {
    CYAN("Ice Blue", Color(0xFF4DDAFF)),
    PURPLE("Neon Violet", Color(0xFFB995FF)),
    GREEN("Mint Pulse", Color(0xFF41E6B2))
}

@Composable
fun MonitorUi(
    unlocked: Boolean,
    snapshot: SystemSnapshot,
    history: List<SystemSnapshot>,
    accent: Accent,
    refreshSeconds: Int,
    onLogin: (String) -> Boolean,
    onLogout: () -> Unit,
    onRefresh: () -> Unit,
    onRefreshSecondsChange: (Int) -> Unit,
    onAccentChange: (Accent) -> Unit,
    onOpenSettings: (String) -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    MaterialTheme(colorScheme = darkColorScheme(primary = accent.tone, background = Navy, surface = Surface)) {
        Box(Modifier.fillMaxSize().background(Navy)) {
            AuroraBackground(accent.tone)
            Crossfade(targetState = unlocked, animationSpec = tween(500), label = "login transition") { loggedIn ->
                if (!loggedIn) {
                    LoginScreen(accent.tone, onLogin)
                } else {
                    MonitorDashboard(
                        snapshot, history, accent, refreshSeconds, onLogout, onRefresh,
                        onRefreshSecondsChange, onAccentChange, onOpenSettings, onCopy, onShare
                    )
                }
            }
        }
    }
}

@Composable
private fun AuroraBackground(accent: Color) {
    Canvas(Modifier.fillMaxSize()) {
        drawRect(brush = Brush.verticalGradient(listOf(Color(0xFF0A1222), Navy, Color(0xFF060A11))))
        drawCircle(
            brush = Brush.radialGradient(
                listOf(accent.copy(alpha = 0.16f), Color.Transparent),
                center = Offset(size.width * 0.9f, size.height * 0.13f),
                radius = size.width * 0.95f
            ),
            radius = size.width * 0.95f,
            center = Offset(size.width * 0.9f, size.height * 0.13f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFF5E50C7).copy(alpha = .12f), Color.Transparent),
                center = Offset(0f, size.height * 0.8f), radius = size.width
            ),
            radius = size.width,
            center = Offset(0f, size.height * 0.8f)
        )
    }
}

@Composable
private fun LoginScreen(accent: Color, onLogin: (String) -> Boolean) {
    var key by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val pulse = rememberInfiniteTransition(label = "login glow")
    val scale by pulse.animateFloat(1f, 1.065f, infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "scale")
    fun submit() {
        focus.clearFocus()
        error = !onLogin(key)
    }

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(35.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            LogoMark(accent, size = 35)
            Spacer(Modifier.width(10.dp))
            Text("AUJUNPEAK", fontSize = 14.sp, letterSpacing = 3.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryText)
            Spacer(Modifier.weight(1f))
            TinyLabel("v1.0", accent)
        }
        Spacer(Modifier.height(55.dp))
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(142.dp)) {
            Box(
                Modifier.size(139.dp).scale(scale).border(1.dp, accent.copy(alpha = .24f), CircleShape)
            )
            Box(Modifier.size(115.dp).background(accent.copy(alpha = .08f), CircleShape).border(1.dp, accent.copy(alpha = .4f), CircleShape))
            Icon(Icons.Rounded.Shield, null, tint = accent, modifier = Modifier.size(53.dp))
            Box(Modifier.align(Alignment.BottomEnd).offset((-2).dp, (-6).dp).size(32.dp).background(Mint, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Lock, null, tint = Navy, modifier = Modifier.size(17.dp))
            }
        }
        Spacer(Modifier.height(29.dp))
        Text("POWER IN", color = PrimaryText, fontSize = 33.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Black)
        Text("YOUR HANDS.", color = accent, fontSize = 33.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(12.dp))
        Text(
            "Trung tâm giám sát thiết bị với giao diện\nthời thượng, tinh gọn và trực quan.",
            fontSize = 13.sp, color = Muted, lineHeight = 20.sp, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(33.dp))
        GlassPanel(modifier = Modifier.fillMaxWidth(), pad = 20.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Key, null, tint = accent, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(10.dp))
                Text("KÍCH HOẠT KEY", color = PrimaryText, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.4.sp)
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = key,
                onValueChange = { key = it; error = false },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Nhập key truy cập...", color = Muted, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Rounded.VpnKey, null, tint = Muted, modifier = Modifier.size(19.dp)) },
                trailingIcon = {
                    IconButton(onClick = { visible = !visible }) {
                        Icon(if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, "Hiện / ẩn key", tint = Muted, modifier = Modifier.size(19.dp))
                    }
                },
                visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accent, unfocusedBorderColor = StrokeGrey,
                    focusedTextColor = PrimaryText, unfocusedTextColor = PrimaryText,
                    cursorColor = accent
                )
            )
            if (error) {
                Spacer(Modifier.height(8.dp))
                Text("Key không hợp lệ. Hãy kiểm tra lại.", color = Color(0xFFFF7E91), fontSize = 12.sp)
            }
            Spacer(Modifier.height(16.dp))
            GradientAction("MỞ KHÓA DASHBOARD", accent, Icons.Rounded.ArrowForward, onClick = { submit() })
            Spacer(Modifier.height(13.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.VerifiedUser, null, tint = Mint, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(5.dp))
                Text("Demo offline · không cần kết nối mạng", fontSize = 10.sp, color = Muted)
            }
        }
        Spacer(Modifier.height(27.dp))
        Text("DESIGNED FOR THE NEXT GENERATION", fontSize = 9.sp, color = Muted.copy(alpha = .7f), letterSpacing = 2.sp)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun MonitorDashboard(
    snapshot: SystemSnapshot,
    history: List<SystemSnapshot>,
    accent: Accent,
    refreshSeconds: Int,
    onLogout: () -> Unit,
    onRefresh: () -> Unit,
    onRefreshSecondsChange: (Int) -> Unit,
    onAccentChange: (Accent) -> Unit,
    onOpenSettings: (String) -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    var section by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 21.dp, end = 18.dp, top = 12.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LogoMark(accent.tone, 39)
            Spacer(Modifier.width(11.dp))
            Column {
                Text("AUJUNPEAK", color = PrimaryText, letterSpacing = 2.sp, fontSize = 15.sp, fontWeight = FontWeight.Black)
                Text("SYSTEM MONITOR", color = Muted, letterSpacing = 1.6.sp, fontSize = 9.sp)
            }
            Spacer(Modifier.weight(1f))
            Row(Modifier.clip(RoundedCornerShape(20.dp)).background(Mint.copy(alpha = .1f)).padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                LiveDot()
                Spacer(Modifier.width(6.dp))
                Text("LIVE", color = Mint, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            Crossfade(targetState = section, animationSpec = tween(250), label = "tab") { target ->
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp).padding(bottom = 18.dp)
                ) {
                    when (target) {
                        0 -> HomeScreen(snapshot, history, accent.tone, refreshSeconds)
                        1 -> AnalyticsScreen(snapshot, history, accent.tone)
                        2 -> ToolsScreen(snapshot, accent.tone, onRefresh, onOpenSettings, onCopy, onShare)
                        else -> PreferencesScreen(accent, refreshSeconds, onAccentChange, onRefreshSecondsChange, onLogout)
                    }
                }
            }
        }
        BottomNavigation(section, accent.tone, onSelected = { section = it })
    }
}

@Composable
private fun BottomNavigation(selected: Int, accent: Color, onSelected: (Int) -> Unit) {
    val items = listOf(
        Triple("Trang chủ", Icons.Rounded.Dashboard, 0),
        Triple("Biểu đồ", Icons.Rounded.ShowChart, 1),
        Triple("Công cụ", Icons.Rounded.AutoFixHigh, 2),
        Triple("Cài đặt", Icons.Rounded.Tune, 3)
    )
    Row(
        Modifier.fillMaxWidth().background(Color(0xF50D1625))
            .border(BorderStroke(0.5.dp, StrokeGrey.copy(alpha = .65f)))
            .padding(start = 6.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEach { (label, icon, index) ->
            val active = selected == index
            val color by animateColorAsState(if (active) accent else Muted, label = "nav color")
            Column(
                Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                    .clickable { onSelected(index) }
                    .background(if (active) accent.copy(alpha = .1f) else Color.Transparent)
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(21.dp))
                Spacer(Modifier.height(3.dp))
                Text(label, fontSize = 10.sp, color = color, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@Composable
private fun HomeScreen(s: SystemSnapshot, history: List<SystemSnapshot>, accent: Color, refreshSeconds: Int) {
    SectionTitle("TỔNG QUAN", "Hiệu năng thiết bị trong thời gian thực")
    Spacer(Modifier.height(14.dp))
    GlassPanel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                TinyLabel("SYSTEM STATUS", accent)
                Spacer(Modifier.height(12.dp))
                Text("Ổn định", color = PrimaryText, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(5.dp))
                Text("Dữ liệu cập nhật mỗi ${refreshSeconds}s", color = Muted, fontSize = 11.sp)
                Spacer(Modifier.height(15.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LiveDot()
                    Spacer(Modifier.width(6.dp))
                    Text("ĐANG THEO DÕI", color = Mint, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = .8.sp)
                }
            }
            RingGauge(s.ramPercent, accent, 119, "RAM", "%.0f%%".format(Locale.US, s.ramPercent))
        }
    }
    Spacer(Modifier.height(12.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.weight(1f)) {
            MiniMetric("CPU ỨNG DỤNG", "%.1f%%".format(Locale.US, s.cpuAppPercent), "Tải tiến trình", Icons.Rounded.Memory, accent, s.cpuAppPercent)
        }
        Box(Modifier.weight(1f)) {
            MiniMetric("RAM THIẾT BỊ", "%.1f%%".format(Locale.US, s.ramPercent), readableSize(s.ramUsedBytes), Icons.Rounded.Storage, Mint, s.ramPercent)
        }
    }
    Spacer(Modifier.height(17.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("RAM HISTORY", color = PrimaryText, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.weight(1f))
        TinyLabel("60 MẪU GẦN NHẤT", accent)
    }
    Spacer(Modifier.height(10.dp))
    GraphCard(history.map { it.ramPercent }, accent, "RAM thiết bị", "%.1f%%".format(Locale.US, s.ramPercent))
    Spacer(Modifier.height(16.dp))
    Text("PHẦN CỨNG", color = PrimaryText, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.sp)
    Spacer(Modifier.height(11.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.weight(1f)) { SmallInfo("PIN", "${s.batteryPercent}%", if (s.charging) "Đang sạc" else "Đang sử dụng", Icons.Rounded.BatteryFull, Mint) }
        Box(Modifier.weight(1f)) { SmallInfo("BỘ NHỚ", "%.0f%%".format(Locale.US, s.storagePercent), "Phân vùng data", Icons.Rounded.SdStorage, Amber) }
    }
    Spacer(Modifier.height(11.dp))
    GlassPanel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Smartphone, null, tint = accent, modifier = Modifier.size(27.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("${Build.MANUFACTURER} ${Build.MODEL}", color = PrimaryText, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                Text("Android ${Build.VERSION.RELEASE}  ·  ${Runtime.getRuntime().availableProcessors()} lõi CPU", color = Muted, fontSize = 11.sp)
            }
            Icon(Icons.Rounded.CheckCircle, null, tint = Mint, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun AnalyticsScreen(s: SystemSnapshot, history: List<SystemSnapshot>, accent: Color) {
    SectionTitle("PHÂN TÍCH", "Biểu đồ hiệu suất trực tiếp")
    Spacer(Modifier.height(15.dp))
    GraphCard(history.map { it.cpuAppPercent }, accent, "CPU của ứng dụng", "%.1f%%".format(Locale.US, s.cpuAppPercent), tall = true)
    Spacer(Modifier.height(14.dp))
    GraphCard(history.map { it.ramPercent }, Mint, "RAM toàn thiết bị", "%.1f%%".format(Locale.US, s.ramPercent), tall = true)
    Spacer(Modifier.height(14.dp))
    GlassPanel {
        Text("CHI TIẾT TÀI NGUYÊN", color = PrimaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(15.dp))
        ResourceLine("RAM đã dùng", readableSize(s.ramUsedBytes), s.ramPercent, accent)
        Spacer(Modifier.height(15.dp))
        ResourceLine("RAM tổng", readableSize(s.ramTotalBytes), 100f, Mint)
        Spacer(Modifier.height(15.dp))
        ResourceLine("Bộ nhớ đã dùng", readableSize(s.storageUsedBytes), s.storagePercent, Amber)
        Spacer(Modifier.height(15.dp))
        ResourceLine("Bộ nhớ tổng", readableSize(s.storageTotalBytes), 100f, accent)
    }
    Spacer(Modifier.height(13.dp))
    GlassPanel {
        Row(verticalAlignment = Alignment.Top) {
            Icon(Icons.Rounded.Info, null, tint = accent, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                "CPU hiển thị mức sử dụng của chính ứng dụng, quy đổi theo toàn bộ lõi CPU. Android giới hạn khả năng đọc tải CPU toàn hệ thống. Biểu đồ chỉ lưu 60 mẫu trong phiên sử dụng.",
                color = Muted, fontSize = 11.sp, lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun ToolsScreen(
    s: SystemSnapshot, accent: Color, onRefresh: () -> Unit, onOpenSettings: (String) -> Unit,
    onCopy: () -> Unit, onShare: () -> Unit
) {
    SectionTitle("CÔNG CỤ", "Truy cập nhanh các tiện ích thiết bị")
    Spacer(Modifier.height(15.dp))
    Text("TRUNG TÂM ĐIỀU KHIỂN", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp, color = Muted)
    Spacer(Modifier.height(11.dp))
    ToolRow("Làm mới dữ liệu", "Đo lại tài nguyên ngay lập tức", Icons.Rounded.Refresh, accent, onRefresh)
    Spacer(Modifier.height(10.dp))
    ToolRow("Cài đặt thiết bị", "Mở bảng điều khiển của Android", Icons.Rounded.Settings, accent, { onOpenSettings(Settings.ACTION_SETTINGS) })
    Spacer(Modifier.height(10.dp))
    ToolRow("Quản lý dung lượng", "Xem và giải phóng bộ nhớ thủ công", Icons.Rounded.SdStorage, Amber, { onOpenSettings(Settings.ACTION_INTERNAL_STORAGE_SETTINGS) })
    Spacer(Modifier.height(10.dp))
    ToolRow("Tiết kiệm pin", "Mở tùy chọn tiết kiệm năng lượng", Icons.Rounded.BatteryChargingFull, Mint, { onOpenSettings(Settings.ACTION_BATTERY_SAVER_SETTINGS) })
    Spacer(Modifier.height(10.dp))
    ToolRow("Thông tin ứng dụng", "Quản lý ứng dụng trong Android", Icons.Rounded.AppSettingsAlt, accent, { onOpenSettings(Settings.ACTION_APPLICATION_DETAILS_SETTINGS) })
    Spacer(Modifier.height(20.dp))
    Text("BÁO CÁO HỆ THỐNG", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp, color = Muted)
    Spacer(Modifier.height(10.dp))
    GlassPanel {
        Text("Xuất dữ liệu hiện tại", color = PrimaryText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(5.dp))
        Text("Mẫu báo cáo gồm CPU ứng dụng, RAM, dung lượng, pin và thông tin thiết bị.", color = Muted, fontSize = 11.sp, lineHeight = 17.sp)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { CompactAction("Sao chép", Icons.Rounded.ContentCopy, accent, onCopy) }
            Box(Modifier.weight(1f)) { CompactAction("Chia sẻ", Icons.Rounded.Share, accent, onShare) }
        }
    }
    Spacer(Modifier.height(13.dp))
    GlassPanel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Thermostat, null, tint = Amber, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Nhiệt độ pin", color = PrimaryText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Cảm biến pin Android", color = Muted, fontSize = 11.sp)
            }
            Text(s.batteryTempC?.let { "%.1f°C".format(Locale.US, it) } ?: "N/A", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Amber)
        }
    }
}

@Composable
private fun PreferencesScreen(
    accent: Accent, refreshSeconds: Int, onAccentChange: (Accent) -> Unit,
    onRefreshSecondsChange: (Int) -> Unit, onLogout: () -> Unit
) {
    SectionTitle("TÙY CHỈNH", "Cá nhân hóa trải nghiệm của bạn")
    Spacer(Modifier.height(16.dp))
    GlassPanel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Palette, null, tint = accent.tone, modifier = Modifier.size(21.dp))
            Spacer(Modifier.width(9.dp))
            Text("MÀU GIAO DIỆN", color = PrimaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
        Spacer(Modifier.height(15.dp))
        Accent.entries.forEach { option ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp))
                    .background(if (option == accent) option.tone.copy(alpha = .11f) else Color.Transparent)
                    .clickable { onAccentChange(option) }
                    .padding(horizontal = 11.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(21.dp).background(option.tone, CircleShape))
                Spacer(Modifier.width(12.dp))
                Text(option.displayName, color = PrimaryText, modifier = Modifier.weight(1f), fontSize = 13.sp)
                if (option == accent) Icon(Icons.Rounded.CheckCircle, null, tint = option.tone, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.height(4.dp))
        }
    }
    Spacer(Modifier.height(12.dp))
    GlassPanel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Timer, null, tint = accent.tone, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(9.dp))
            Text("TỐC ĐỘ CẬP NHẬT", color = PrimaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
        Spacer(Modifier.height(5.dp))
        Text("Giãn cách lấy mẫu để giảm tiêu thụ pin.", color = Muted, fontSize = 11.sp)
        Spacer(Modifier.height(15.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            listOf(1, 2, 3).forEach { seconds ->
                val selected = refreshSeconds == seconds
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(13.dp))
                        .background(if (selected) accent.tone.copy(alpha = .16f) else SurfaceLight)
                        .border(1.dp, if (selected) accent.tone else StrokeGrey, RoundedCornerShape(13.dp))
                        .clickable { onRefreshSecondsChange(seconds) }.padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${seconds}s", color = if (selected) accent.tone else PrimaryText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
    Spacer(Modifier.height(12.dp))
    GlassPanel {
        Text("THÔNG TIN ỨNG DỤNG", color = PrimaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(12.dp))
        InfoPair("Tên ứng dụng", "Aujunpeak Monitor")
        Spacer(Modifier.height(10.dp))
        InfoPair("Phiên bản", "1.0.0")
        Spacer(Modifier.height(10.dp))
        InfoPair("Key", "Demo offline")
        Spacer(Modifier.height(10.dp))
        InfoPair("Internet", "Không yêu cầu")
    }
    Spacer(Modifier.height(17.dp))
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(Color(0x33FC637B)).border(1.dp, Color(0x66FC637B), RoundedCornerShape(16.dp))
            .clickable(role = Role.Button) { onLogout() }.padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Logout, null, tint = Color(0xFFFF859A), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("ĐĂNG XUẤT KEY", color = Color(0xFFFF859A), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
    Spacer(Modifier.height(17.dp))
    Text("Made with attention to performance & privacy.", color = Muted, fontSize = 10.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
}

@Composable
private fun LogoMark(accent: Color, size: Int) {
    Box(
        Modifier.size(size.dp).clip(RoundedCornerShape((size * .28f).dp))
            .background(Brush.linearGradient(listOf(accent, accent.copy(alpha = .6f)))),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Rounded.MonitorHeart, null, tint = Navy, modifier = Modifier.size((size * .59f).dp))
    }
}

@Composable
private fun LiveDot() {
    val infinite = rememberInfiniteTransition(label = "live")
    val opacity by infinite.animateFloat(.45f, 1f, infiniteRepeatable(tween(1100), RepeatMode.Reverse), label = "live blink")
    Box(Modifier.size(6.dp).alpha(opacity).background(Mint, CircleShape))
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column {
        Text(title, color = PrimaryText, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp, fontSize = 24.sp)
        Spacer(Modifier.height(3.dp))
        Text(subtitle, color = Muted, fontSize = 12.sp)
    }
}

@Composable
private fun TinyLabel(text: String, color: Color) {
    Text(text, color = color, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.sp)
}

@Composable
private fun GlassPanel(modifier: Modifier = Modifier, pad: androidx.compose.ui.unit.Dp = 16.dp, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxWidth().clip(RoundedCornerShape(19.dp))
            .background(Brush.verticalGradient(listOf(SurfaceLight.copy(alpha = .92f), Surface.copy(alpha = .96f))))
            .border(1.dp, StrokeGrey, RoundedCornerShape(19.dp)).padding(pad),
        content = content
    )
}

@Composable
private fun GradientAction(title: String, accent: Color, icon: ImageVector, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp))
            .background(Brush.horizontalGradient(listOf(accent, accent.copy(red = (accent.red * .78f).coerceIn(0f, 1f)))))
            .clickable(role = Role.Button) { onClick() }
            .padding(horizontal = 17.dp, vertical = 17.dp)
    ) {
        Text(title, color = Navy, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.align(Alignment.Center))
        Icon(icon, null, tint = Navy, modifier = Modifier.align(Alignment.CenterEnd).size(19.dp))
    }
}

@Composable
private fun RingGauge(value: Float, accent: Color, diameter: Int, title: String, centerLabel: String) {
    val progress by animateFloatAsState(value.coerceIn(0f, 100f) / 100f, animationSpec = tween(750), label = "gauge")
    Box(Modifier.size(diameter.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val width = 9.dp.toPx()
            val margin = 10.dp.toPx()
            val topLeft = Offset(margin, margin)
            val arcSize = androidx.compose.ui.geometry.Size(size.width - margin * 2, size.height - margin * 2)
            drawArc(StrokeGrey, -90f, 360f, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(width, cap = StrokeCap.Round))
            drawArc(brush = Brush.sweepGradient(listOf(accent.copy(alpha = .25f), accent, accent)), startAngle = -90f,
                sweepAngle = 360f * progress, useCenter = false, topLeft = topLeft, size = arcSize,
                style = Stroke(width, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(centerLabel, color = PrimaryText, fontSize = 25.sp, fontWeight = FontWeight.Black)
            Text(title, color = Muted, fontSize = 10.sp, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun MiniMetric(title: String, value: String, note: String, icon: ImageVector, color: Color, progress: Float) {
    GlassPanel(pad = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(19.dp))
            Spacer(Modifier.weight(1f))
            LiveDot()
        }
        Spacer(Modifier.height(12.dp))
        Text(title, color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = .45.sp)
        Spacer(Modifier.height(5.dp))
        Text(value, color = PrimaryText, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(3.dp))
        Text(note, color = Muted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(13.dp))
        ProgressLine(progress, color)
    }
}

@Composable
private fun SmallInfo(title: String, value: String, extra: String, icon: ImageVector, color: Color) {
    GlassPanel(pad = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(7.dp))
            Text(title, color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(9.dp))
        Text(value, color = PrimaryText, fontWeight = FontWeight.Black, fontSize = 23.sp)
        Text(extra, color = Muted, fontSize = 10.sp)
    }
}

@Composable
private fun ProgressLine(value: Float, color: Color) {
    val animated by animateFloatAsState(value.coerceIn(0f, 100f) / 100f, animationSpec = tween(700), label = "progress")
    Box(Modifier.fillMaxWidth().height(5.dp).clip(CircleShape).background(StrokeGrey)) {
        Box(Modifier.fillMaxWidth(animated).fillMaxHeight().background(color, CircleShape))
    }
}

@Composable
private fun GraphCard(values: List<Float>, color: Color, title: String, value: String, tall: Boolean = false) {
    GlassPanel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = Muted, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                Spacer(Modifier.height(3.dp))
                Text(value, color = PrimaryText, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Timeline, null, tint = color, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(4.dp))
                Text("REAL TIME", color = color, fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = .7.sp)
            }
        }
        Spacer(Modifier.height(14.dp))
        HistoryChart(values, color, if (tall) 176 else 135)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("60 mẫu trước", fontSize = 9.sp, color = Muted)
            Text("Hiện tại", fontSize = 9.sp, color = Muted)
        }
    }
}

@Composable
private fun HistoryChart(values: List<Float>, color: Color, height: Int) {
    Canvas(Modifier.fillMaxWidth().height(height.dp)) {
        val w = size.width
        val h = size.height
        for (index in 0..4) {
            val y = h * index / 4f
            drawLine(StrokeGrey.copy(alpha = .7f), Offset(0f, y), Offset(w, y), strokeWidth = 1.dp.toPx())
        }
        if (values.size >= 2) {
            val px = w / (values.size - 1)
            val pts = values.mapIndexed { index, value ->
                Offset(index * px, h - (value.coerceIn(0f, 100f) / 100f) * h)
            }
            val line = Path().apply {
                moveTo(pts.first().x, pts.first().y)
                pts.drop(1).forEach { lineTo(it.x, it.y) }
            }
            val shade = Path().apply {
                addPath(line)
                lineTo(pts.last().x, h)
                lineTo(pts.first().x, h)
                close()
            }
            drawPath(shade, brush = Brush.verticalGradient(listOf(color.copy(alpha = .28f), color.copy(alpha = .01f))))
            drawPath(line, color = color, style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawCircle(color, radius = 4.dp.toPx(), center = pts.last())
            drawCircle(Navy, radius = 1.8.dp.toPx(), center = pts.last())
        }
    }
}

@Composable
private fun ResourceLine(label: String, amount: String, progress: Float, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Muted, fontSize = 11.sp, modifier = Modifier.weight(1f))
        Text(amount, color = PrimaryText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
    Spacer(Modifier.height(7.dp))
    ProgressLine(progress, color)
}

@Composable
private fun ToolRow(title: String, subtitle: String, icon: ImageVector, accent: Color, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(17.dp))
            .background(Surface).border(1.dp, StrokeGrey, RoundedCornerShape(17.dp))
            .clickable(role = Role.Button) { onClick() }.padding(horizontal = 13.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(43.dp).clip(RoundedCornerShape(12.dp)).background(accent.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = PrimaryText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, color = Muted, fontSize = 10.sp, maxLines = 2)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = Muted, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun CompactAction(title: String, icon: ImageVector, accent: Color, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(accent.copy(alpha = .13f))
            .border(1.dp, accent.copy(alpha = .25f), RoundedCornerShape(12.dp))
            .clickable(role = Role.Button) { onClick() }.padding(vertical = 13.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(7.dp))
        Text(title, color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InfoPair(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Muted, fontSize = 11.sp, modifier = Modifier.weight(1f))
        Text(value, color = PrimaryText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
