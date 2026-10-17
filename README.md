# AUJUNPEAK MONITOR V1.0.1 — Android & GitHub Actions FIX

Android **Kotlin + Jetpack Compose**, dark neon, login key, CPU (tiến trình app), RAM (toàn thiết bị), biểu đồ 60 mẫu, pin, lưu trữ, công cụ hệ thống.

## Hướng dẫn build APK trực tiếp trên GitHub

1. Tạo repository GitHub mới hoặc dùng repository cũ.
2. **Giải nén ZIP và đưa NỘI DUNG bên trong vào THƯ MỤC GỐC** repository (không upload mỗi file `.zip`, không để lồng thêm `AujunpeakMonitor_Fixed_V1.0.1/`).
3. Đảm bảo GitHub hiển thị `app/`, `.github/workflows/build-apk.yml`, `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `scripts/validate_project.py` ở đúng cấp.
4. Commit lên nhánh `main` hoặc `master`. Tab **Actions → Build Android APK** tự chạy, hoặc bấm **Run workflow**.
5. Khi dấu xanh, mở run → **Artifacts → AujunpeakMonitor-V1.0.1-debug-apk** → tải và giải nén để nhận **app-debug.apk**.

**Quan trọng:** Khi cập nhật repository cũ, bạn phải thay file `.github/workflows/build-apk.yml` — **Re-run jobs trên commit cũ sẽ vẫn chạy workflow cũ**, không sửa lỗi. Hãy tạo commit mới / chạy workflow mới sau khi thay file.

## Lỗi GitHub đã xử lý

- Bỏ `android-actions/setup-android@v3` (action cũ có thể gọi package Android SDK `tools` đã bị loại bỏ và thất bại ngay ở bước Set up Android SDK).
- Dùng `ubuntu-24.04` đã có Android SDK 35 và Android Build-tools 35.0.0; nếu thiếu mới gọi `sdkmanager` để cài **chỉ** các gói hỗ trợ.
- Ghép bộ Android Gradle Plugin **8.9.2**, Gradle **8.11.1**, Kotlin Android/Compose compiler **2.2.21**, Java **17** và compileSdk/targetSdk **35**.
- Có kiểm tra cấu hình + XML/key demo trước khi build; check APK khác 0 byte rồi mới upload Artifact. Lệnh Gradle dùng `--stacktrace` để hiển thị lỗi đầy đủ nếu CI vẫn thất bại.
- Không cần Gradle Wrapper trên GitHub: `gradle/actions/setup-gradle@v4` cài Gradle 8.11.1.

## Key demo

```
AUJUN-2026
```

Key được xác minh bằng SHA-256 cục bộ. **Không phải hệ thống quản lý license thương mại** (có thể bị bypass khi reverse-engineer APK); muốn dùng key trả phí cần backend HTTPS xác thực.

## Thông số và lưu ý

- Android từ 8.0 (API 26), package `com.aujunpeak.monitor`.
- Không yêu cầu quyền INTERNET, không thu thập/gửi dữ liệu ra server.
- RAM là RAM toàn thiết bị. CPU là CPU **của app**, quy đổi theo số lõi, không phải tải CPU toàn máy. Nhiệt độ pin không phải nhiệt độ CPU.
- Hệ thống cập nhật theo lựa chọn 1/2/3 giây; tự dừng lấy mẫu khi đăng xuất.
- APK đầu ra là **debug** được ký để thử nghiệm, không phải APK release đã ký để phân phối chính thức.

## Kiểm tra cục bộ

Từ thư mục gốc:

```bash
python3 scripts/validate_project.py
```

Nếu có Android SDK và Gradle 8.11.1:

```bash
gradle :app:assembleDebug --no-daemon --stacktrace
```

### Những gì đã và chưa được kiểm chứng

- Đã kiểm tra tĩnh: tính nhất quán cấu hình workflow/Gradle, cấu trúc module, manifest + XML và SHA-256 key.
- Đã biên dịch & chạy kiểm thử JVM cho class `KeyGate.kt` với 4 test case, đều đạt.
- **Chưa build APK Android trong môi trường tạo ZIP này**, do không có Android SDK và không có kết nối tải dependencies từ Google/Maven. Kết quả thực tế cần được xác nhận bằng GitHub Actions; nếu lỗi bước Gradle, mở log `Build Android debug APK` và gửi khoảng 30 dòng cuối của lỗi.
