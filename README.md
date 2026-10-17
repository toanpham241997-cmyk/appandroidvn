# AUJUNPEAK MONITOR · Android

Ứng dụng **Android native bằng Kotlin + Jetpack Compose**: màn hình login key, dashboard dark-tech, biểu đồ sử dụng RAM/CPU, các công cụ hệ thống và tuỳ chỉnh màu sắc. Giao diện bằng tiếng Việt; **không yêu cầu quyền Internet**.

## 🗝 Key demo

```text
AUJUN-2026
```

Chỉ là demo key **offline**. Mã kiểm tra SHA-256 nằm ở `app/src/main/java/com/aujunpeak/monitor/auth/KeyGate.kt`. Đăng nhập hợp lệ được lưu cục bộ bằng SharedPreferences. **Không dùng cơ chế này để bán key bản quyền**: bất kỳ kiểm tra offline nào cũng có thể bị giả mạo hoặc bỏ qua khi phân tích APK. Muốn triển khai key trả phí, cần một backend HTTPS để xác minh key, thời hạn, số thiết bị và cấp phiên làm việc.

## 📱 Chức năng

- Login bằng key, hiển thị/ẩn key, ghi nhớ đăng nhập, đăng xuất.
- Dashboard tối, gradient xanh/tím, hiệu ứng chuyển tab, nhịp LIVE, gauge RAM và đồ thị thật cập nhật theo thời gian.
- **RAM toàn thiết bị**: đọc `ActivityManager.MemoryInfo`.
- **CPU của riêng ứng dụng**: đọc `Process.getElapsedCpuTime`, quy đổi theo tổng số lõi. Đây **không phải CPU tổng toàn máy**; Android hạn chế số liệu CPU toàn máy cho app thông thường.
- **Bộ nhớ trong**: dung lượng phân vùng dữ liệu theo `StatFs`.
- **Pin**: phần trăm, tình trạng sạc và nhiệt độ cảm biến pin (nếu có).
- Thông tin Android, model, số lõi CPU, uptime.
- Đồ thị lưu **60 mẫu gần nhất** trong phiên, làm mới mỗi 1 / 2 / 3 giây.
- Công cụ mở Cài đặt Android, quản lý bộ nhớ, cài đặt pin, thông tin ứng dụng.
- Sao chép hoặc chia sẻ báo cáo hệ thống qua các ứng dụng khác.
- Cấu hình 3 màu neon: Ice Blue, Neon Violet và Mint Pulse.

## 🚀 Build APK trực tiếp trên GitHub (không cần Android Studio)

1. Tạo repository mới trên GitHub, ví dụ `AujunpeakMonitor`.
2. Giải nén ZIP này và **đẩy toàn bộ nội dung thư mục `AujunpeakMonitor` lên gốc repository**. Cần giữ nguyên đường dẫn `.github/workflows/build-apk.yml`.
3. Khi push vào `main` hoặc `master`, workflow **Build Android APK** tự chạy. Cũng có thể vào tab **Actions → Build Android APK → Run workflow**.
4. Mở lần chạy thành công, kéo xuống mục **Artifacts**, tải `AujunpeakMonitor-debug-apk`.
5. Giải nén artifact để nhận **`app-debug.apk`**, chuyển lên điện thoại Android và cài đặt.

Nếu cần đẩy mã qua dòng lệnh (thay `USERNAME`):

```bash
git init
git add .
git commit -m "Initial Aujunpeak Monitor app"
git branch -M main
git remote add origin https://github.com/USERNAME/AujunpeakMonitor.git
git push -u origin main
```

**Lưu ý:** Artifact APK là **debug APK đã ký bằng debug key**, cài thử được, không phải bản phát hành ký chính thức để đăng Play Store. Build lần đầu cần tải Gradle, Android SDK và dependencies trên GitHub Actions.

## 🧰 Yêu cầu kỹ thuật

- Android 8.0 trở lên (minSdk 26).
- targetSdk 35 / compileSdk 35.
- Android Gradle Plugin 8.9.2, Gradle 8.11.1, Kotlin 2.3.21, Jetpack Compose.
- Workflow dùng `ubuntu-latest`, Java 17, Android SDK 35.

## ⚙️ Mở trong Android Studio

Chọn **Open → AujunpeakMonitor**, chọn JDK 17 và Gradle 8.11.1. Dự án cố ý không chứa `gradlew`/wrapper jar; GitHub Actions cài Gradle phiên bản ghim sẵn. Nếu Android Studio yêu cầu wrapper, có thể tạo wrapper với Gradle 8.11.1 hoặc chỉ định Gradle cài trên máy.

## 🔏 Quyền riêng tư và giới hạn

Không có `INTERNET` permission và không gửi dữ liệu đi đâu. Clipboard/Share chỉ thao tác khi người dùng chạm nút. Dữ liệu cảm biến tùy thiết bị. Con số RAM là RAM **toàn thiết bị**, CPU là **tiến trình ứng dụng**, nhiệt độ là **pin** (không phải nhiệt độ CPU), lưu trữ là **phân vùng data**.

**Phiên bản này là mã nguồn có quy trình build tự động; không kèm APK build sẵn.**
