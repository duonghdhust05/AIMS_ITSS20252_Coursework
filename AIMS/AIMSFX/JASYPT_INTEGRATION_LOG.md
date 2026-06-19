# Nhật ký Tích hợp Jasypt - Bảo mật Secrets cho Installer

Tài liệu này lưu lại toàn bộ quá trình trao đổi và các bước thực hiện để tích hợp Jasypt vào dự án AIMS, nhằm bảo mật thông tin nhạy cảm (secrets) khi đóng gói installer.

## 1. Yêu cầu ban đầu
- **Mục tiêu**: Sử dụng Jasypt để mã hóa toàn bộ các thông tin nhạy cảm (secrets) và đóng gói vào installer.
- **Ràng buộc**:
  - Cấu hình Jasypt với một key cố định (nhúng trực tiếp trong code).
  - Đảm bảo người dùng cuối cài đặt xong có thể chạy ngay mà không cần tinh chỉnh hay nhập key.
  - Các secrets tuyệt đối không được lộ ra ở định dạng văn bản gốc (plaintext) ở bất cứ file cấu hình nào trong thư mục cài đặt.

## 2. Khảo sát & Phân tích hiện trạng
- Thông qua việc tìm kiếm mã nguồn, hệ thống phát hiện các file cấu hình và class đang chứa hoặc sử dụng các thông tin nhạy cảm:
  - File **`application.properties`** lưu giữ nhiều secret: `spring.datasource.password`, `vietqr.client.password`, `app.jwt.secret`, `paypal.client.secret`, `email.password`.
  - Dự án kết hợp Spring Boot và JavaFX. Mặc dù có cấu hình Spring Boot, nhưng nhiều Subsystem/Class (như `DatabaseConnection`, `VietQRConfig`, `PayPalConfig`, `EmailService`, `EmailSenderService`) không sử dụng `@Value` của Spring mà trực tiếp dùng `Properties.load()` để đọc từ `application.properties`.

## 3. Lên Kế hoạch Triển khai (Implementation Plan)
Để đảm bảo tất cả bí mật được bảo vệ toàn diện (cả ở Spring bean lẫn code thủ công):
- **Thêm Dependency**: Bổ sung `jasypt-spring-boot-starter` vào file `pom.xml`.
- **Lập cấu hình Jasypt**: Tạo class `JasyptConfig` chứa khóa bí mật tĩnh (`AimsSecretKey20252!@`). Class này sẽ expose Spring Bean `StringEncryptor` để tự động xử lý `@Value`, đồng thời cung cấp phương thức static `decryptProperty()` cho các logic đọc file thủ công.
- **Mã hóa dữ liệu**: Sinh chuỗi mã hóa cho tất cả các plaintext password.
- **Thay thế File Cấu Hình**: Thay các password gốc trong `application.properties` thành định dạng `ENC(...)`.
- **Refactor Code**: Cập nhật các class đang gọi `props.getProperty(...)` sử dụng thêm hàm bọc `JasyptConfig.decryptProperty(...)`.

## 4. Quá trình Thực thi (Execution)
### Bước 4.1: Cập nhật `pom.xml`
Thêm dependency Jasypt:
```xml
<dependency>
    <groupId>com.github.ulisesbocchio</groupId>
    <artifactId>jasypt-spring-boot-starter</artifactId>
    <version>3.0.5</version>
</dependency>
```

### Bước 4.2: Tạo `JasyptConfig.java`
Đoạn code nhúng key tĩnh và cấu hình Jasypt:
```java
@Configuration
public class JasyptConfig {
    public static final String JASYPT_KEY = "AimsSecretKey20252!@";
    private static StandardPBEStringEncryptor encryptor;
    
    // ... Khởi tạo encryptor và Bean stringEncryptor ...

    public static String decryptProperty(String encryptedValue) {
        if (encryptedValue != null && encryptedValue.startsWith("ENC(") && encryptedValue.endsWith(")")) {
            // ... Logic giải mã bằng JASYPT_KEY ...
        }
        return encryptedValue;
    }
}
```

### Bước 4.3: Mã hóa Secrets
Tạo class `JasyptTest.java` để thực hiện mã hóa các string plaintext. Các chuỗi `ENC(...)` được sinh ra tương ứng như sau:
- DB: `ENC(0h2O+7/AaXFT7ZZR6uD51SGO+hoLDWfv)`
- VietQR: `ENC(ft4z6LR8KvEa8GATwZNpq+jOPBPUsDP0voag5f4Vybv5wQtywuSqffwWWL/x2I3cCphBYGrvgcA=)`
- JWT: `ENC(d48waR2ocOw+PDfbo/27gBYS2hKN7TEnvYbv4RU4ahgh1llLqxXciZY6o8IqQwpH)`
- PayPal: `ENC(Ny5yvX4cBECCi7hpHwVT2amM8lUnTJPn9z48Rb9dmpgS6IihyOwnF2MAW2n6F7m5G2A81pPFuYOguw5Vi86PJFJV/spNZmjl9FUaAJ6JMDlUBdIyM+UunLpdWzJNaUBD)`
- Email: `ENC(QHd/W/8s5XascqGD1YAjbCdYTnD3seLW2HHkJX3fjjY=)`

### Bước 4.4: Thay thế vào `application.properties`
Các password văn bản thuần được thay thế hoàn toàn bằng chuỗi mã hóa `ENC(...)`.

### Bước 4.5: Refactor Code Load Thủ công
Cập nhật các class để tự động gọi hàm giải mã:
- **`DatabaseConnection.java`**:
  `this.password = JasyptConfig.decryptProperty(props.getProperty("spring.datasource.password"));`
- Tương tự với **`VietQRConfig.java`**, **`PayPalConfig.java`**, **`EmailService.java`**, và **`EmailSenderService.java`**. Đồng thời dọn dẹp các thư viện thừa (unused imports).

## 5. Kiểm thử & Xác nhận (Verification)
- **Phương pháp**: Chạy lệnh `mvn compile test -DskipTests=false` để build project và chạy toàn bộ Unit Tests.
- **Kết quả**: 
  - Toàn bộ **181 tests passed** (0 Failures, 0 Errors).
  - Test `DatabaseTest` in ra logs: `SUCCESS: Connection pool initialized successfully!`, chứng tỏ HikariCP đã kết nối thành công tới Database Supabase.
  - Các hệ thống VietQR, PayPal, và Email khởi tạo thành công bằng cách sử dụng secret đã được giải mã động trong quá trình chạy.
- **Hoàn thành**: 100% mục tiêu ban đầu. Người dùng nay có thể build installer an toàn mà không sợ lộ plaintext passwords bên trong file settings.
