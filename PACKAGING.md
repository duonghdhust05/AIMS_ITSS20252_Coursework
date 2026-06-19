# Hướng dẫn Đóng gói và Tạo Bộ cài đặt (.exe) cho dự án AIMS

Tài liệu này hướng dẫn chi tiết cách biên dịch dự án và đóng gói ứng dụng **Spring Boot + JavaFX (AIMS)** thành file cài đặt chạy trực tiếp trên hệ điều hành Windows (`.exe` / `.msi`) bằng công cụ `jpackage` tích hợp sẵn trong JDK.

---

## 1. Yêu cầu hệ thống (Prerequisites)
Để thực hiện việc đóng gói, máy tính của bạn cần được cấu hình các công cụ sau:

* **JDK 21 trở lên** (Ví dụ: `jdk-26.0.1` đã cài ở đường dẫn `C:\Program Files\Java\jdk-26.0.1`).
* **Apache Maven** (Ví dụ đã giải nén tại `C:\Program Files\apache-maven-3.9.16`).
* **Cấu hình Biến môi trường (Environment Variables):**
  * `JAVA_HOME` trỏ tới: `C:\Program Files\Java\jdk-26.0.1`
  * `Path` hệ thống phải chứa đường dẫn: `%JAVA_HOME%\bin` (hoặc `C:\Program Files\Java\jdk-26.0.1\bin`) và đường dẫn tới thư mục `bin` của Maven (`C:\Program Files\apache-maven-3.9.16\bin`).

---

## 2. Quy trình đóng gói 2 bước

Luôn mở **Terminal (PowerShell hoặc CMD)** tại thư mục con chứa dự án **`AIMS/AIMSFX`** trước khi chạy lệnh.

### Bước 1: Biên dịch mã nguồn ra file `.jar`
Chạy lệnh Maven dưới đây để dọn dẹp và đóng gói code thành file Fat Jar (chứa toàn bộ code và thư viện đi kèm):
```bash
mvn clean package -DskipTests
```
Sau khi build thành công (`BUILD SUCCESS`), file jar thực thi sẽ nằm tại:
`AIMS/AIMSFX/target/AIMSFX-1.0-SNAPSHOT-exec.jar`

### Bước 2: Tạo bộ cài đặt (.exe) bằng `jpackage`
Chạy lệnh dưới đây để đóng gói file `.jar` thành file `.exe` cài đặt:

```powershell
jpackage --type exe --name AIMSFX --input target/ --main-jar AIMSFX-1.0-SNAPSHOT-exec.jar --dest output-installer --win-shortcut --win-menu --icon src/main/resources/com/aimsfx/aims-icon.ico --runtime-image "C:\Program Files\Java\jdk-26.0.1"
```

* **Giải thích các tham số chính:**
  * `--type exe`: Định dạng xuất ra là bộ cài đặt Windows `.exe`.
  * `--name AIMSFX`: Tên của ứng dụng khi cài đặt vào máy.
  * `--input target/`: Thư mục đầu vào chứa file jar.
  * `--main-jar AIMSFX-1.0-SNAPSHOT-exec.jar`: File jar thực thi chính của Spring Boot.
  * `--dest output-installer`: Thư mục đầu ra sẽ chứa file cài đặt `.exe`.
  * `--win-shortcut` & `--win-menu`: Tự động tạo biểu tượng ứng dụng ngoài Desktop và Menu Start sau khi cài.
  * `--icon ...`: Đường dẫn tới file ảnh biểu tượng ứng dụng dạng `.ico`.
  * `--runtime-image "C:\Program Files\Java\jdk-26.0.1"`: Copy nguyên bản môi trường JDK hiện tại vào app để chạy ổn định mọi tính năng của Spring Boot & Database (không bị cắt tỉa module).

---

## 3. Theo dõi Log và Sửa lỗi (Troubleshooting)

### Xuất Log ra file để kiểm tra
Ứng dụng được cấu hình tự động lưu lại toàn bộ log khởi động, kết nối Database (Supabase) và lỗi crash vào file sau:
`C:\Users\<Tên_User>\aims-app.log`

Bạn có thể mở trực tiếp file này bằng Notepad để kiểm tra khi phần mềm hoạt động không đúng ý muốn.

### Bật Console đen (CMD) khi mở app để Debug
Nếu ứng dụng bị lỗi ngay khi bật lên mà không rõ nguyên nhân, bạn có thể đóng gói lại ứng dụng kèm cửa sổ Console bằng cách thêm cờ `--win-console` vào lệnh `jpackage`:

```powershell
jpackage --type exe --name AIMSFX --input target/ --main-jar AIMSFX-1.0-SNAPSHOT-exec.jar --dest output-installer --win-shortcut --win-menu --icon src/main/resources/com/aimsfx/aims-icon.ico --runtime-image "C:\Program Files\Java\jdk-26.0.1" --win-console
```
Khi chạy file cài đặt này, một màn hình dòng lệnh sẽ luôn mở kèm để hiển thị toàn bộ log lỗi trực tiếp trước khi crash.

---

*Tài liệu được khởi tạo tự động bởi trợ lý AI Antigravity.*
