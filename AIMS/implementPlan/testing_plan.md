# Đề xuất Kế hoạch Kiểm thử Toàn diện (Comprehensive Testing Plan)

Tài liệu này cung cấp một chiến lược kiểm thử toàn diện cho hệ thống AIMS, bao trùm cả hai khía cạnh chức năng (Functional) và phi chức năng (Non-functional). Đồng thời, tài liệu đưa ra các bộ chỉ số đo lường (Metrics) để đánh giá chất lượng hệ thống ở mức độ đồ án môn học (Coursework) và tiêu chuẩn thực tế (Production-Ready).

---

## 1. Kiểm thử Chức năng (Functional Testing)

Đảm bảo hệ thống thực hiện đúng và đủ các chức năng nghiệp vụ theo yêu cầu.

### 1.1. Unit Testing (Kiểm thử Mức Đơn vị)
- **Mục tiêu**: Đảm bảo từng hàm, từng class nghiệp vụ hoạt động chính xác một cách độc lập.
- **Phạm vi**:
  - `Validators`: Kiểm tra tính đúng đắn của dữ liệu đầu vào (ví dụ: `CommonProductValidator` cho việc kiểm tra định dạng kích thước, dải giá hợp lệ).
  - `Services`: Kiểm tra logic tính phí vận chuyển (`WeightBasedFeeCalculator`), logic tạo mã giao dịch.
  - `Factories`: Kiểm tra việc khởi tạo đúng đối tượng sản phẩm dựa trên loại (`ProductFactoryRegistry`).
- **Kỹ thuật**: Blackbox (Boundary Value Analysis, Equivalence Partitioning) và Whitebox (Branch Coverage).

### 1.2. Integration Testing (Kiểm thử Tích hợp)
- **Mục tiêu**: Đảm bảo các module giao tiếp trơn tru với nhau và với các hệ thống bên ngoài.
- **Phạm vi**:
  - **Database Integration**: Đảm bảo luồng tạo đơn hàng lưu đúng dữ liệu xuống các bảng `orders`, `order_items`, `delivery_info` qua `OrderRepository` cùng với cơ chế Rollback khi có lỗi.
  - **External Gateway Integration**: Tích hợp với `PayPalSubsystem` và `VietQRSubsystem`. Kiểm tra việc tạo request và xử lý response (bao gồm cả việc map các HTTP errors thành `PaymentException` của hệ thống).
- **Kỹ thuật**: Sử dụng Database Test (như H2 in-memory) hoặc WireMock để giả lập phản hồi từ PayPal API.

### 1.3. System & End-to-End (E2E) Testing
- **Mục tiêu**: Kiểm tra toàn bộ luồng nghiệp vụ từ góc độ người dùng cuối.
- **Phạm vi**: 
  - **Luồng Checkout**: Thêm vào giỏ -> Xem giỏ hàng -> Điền thông tin giao hàng -> Chọn phương thức thanh toán -> Chuyển hướng thanh toán -> Xử lý kết quả -> Gửi email xác nhận -> Hiển thị màn hình thành công.
- **Kỹ thuật**: Test kịch bản (Scenario-based testing), bao gồm cả Happy Path (Thành công) và Unhappy Path (Thẻ hết tiền, Hủy thanh toán).

### 1.4. GUI / UI Testing
- **Mục tiêu**: Đảm bảo giao diện người dùng JavaFX hiển thị đúng, các nút bấm hoạt động và phản hồi chính xác.
- **Phạm vi**: Validation form đầu vào (báo lỗi chữ đỏ khi nhập sai format email), cập nhật giỏ hàng trực tiếp không cần reload, hiển thị đúng màn hình loading (spinner) khi đợi gọi API thanh toán.

---

## 2. Kiểm thử Phi chức năng (Non-Functional Testing)

Đảm bảo hệ thống đáp ứng các tiêu chuẩn về hiệu năng, bảo mật, và trải nghiệm người dùng.

### 2.1. Performance & Load Testing (Hiệu năng và Chịu tải)
- **Mục tiêu**: Hệ thống duy trì tốc độ phản hồi tốt ngay cả khi có nhiều người truy cập.
- **Phạm vi**:
  - **Response Time**: Thời gian tải danh sách sản phẩm trang chủ, thời gian tính toán phí ship.
  - **Connection Pool**: Kiểm tra giới hạn số lượng kết nối tối đa của HikariCP (`maxConnections`) dưới áp lực giả lập của 100-500 requests đồng thời.

### 2.2. Security Testing (Bảo mật)
- **Mục tiêu**: Bảo vệ dữ liệu người dùng và ngăn chặn các cuộc tấn công phổ biến.
- **Phạm vi**:
  - **SQL Injection**: Đảm bảo 100% các câu truy vấn Database đều sử dụng `PreparedStatement` thay vì cộng chuỗi (`+`).
  - **Sensitive Data**: Đảm bảo không log thông tin nhạy cảm (như thẻ tín dụng, mật khẩu) ra console. File `application.properties` chứa mật khẩu CSDL không được commit lên public repository.
  - **Data Integrity**: Ngăn chặn tấn công thay đổi giá sản phẩm từ phía Client trước khi gửi request Checkout.

### 2.3. Reliability & Robustness (Độ tin cậy và Tính bền bỉ)
- **Mục tiêu**: Khả năng phục hồi của hệ thống khi gặp sự cố không mong muốn.
- **Phạm vi**:
  - **Network Failure**: Khi đang gọi API PayPal mà rớt mạng, hệ thống có bị treo (freeze UI) hay hiển thị thông báo lỗi thân thiện và cho phép thử lại?
  - **Database Timeout**: Xử lý ngoại lệ khi CSDL quá tải, đưa ra cảnh báo bảo trì thay vì crash toàn bộ ứng dụng (`NullPointerException`).

### 2.4. Usability Testing (Tính khả dụng)
- **Phạm vi**: Hệ thống có đưa ra thông báo rõ ràng khi giỏ hàng trống? Form nhập liệu có hỗ trợ Tab navigation và focus đúng ô lỗi?

---

## 3. Chỉ số Đánh giá (Evaluation Metrics)

Để đánh giá chất lượng hệ thống, chúng ta phân tách thành 2 cấp độ: Đồ án môn học và Môi trường thực tế (Production).

### 3.1. Cấp độ Đồ án môn học (Coursework Level)
Mục tiêu là chứng minh hiểu biết về quy trình phát triển và hoàn thiện tính năng cốt lõi.

| Khía cạnh | Chỉ số Đánh giá (Metrics) | Tiêu chuẩn Đạt |
| :--- | :--- | :--- |
| **Functional** | Số lượng Unit Tests | Tối thiểu có viết Unit Test cho các Services và Validators cốt lõi. |
| | Code Coverage (Độ phủ mã) | **> 60%** cho các package lõi (`service`, `validator`). |
| | Happy Path Completion | 100% các luồng chức năng chính (Mua hàng, Tính phí, Thanh toán) chạy thành công không có bug chặn (Blocker). |
| **Non-functional**| Ổn định giao diện (UI Stability) | Không ném ra ngoại lệ trên Console khi người dùng thao tác sai. |
| | Tính toàn vẹn Dữ liệu cơ bản | Sử dụng đúng `PreparedStatement` để phòng chống lỗi syntax SQL/Injection cơ bản. |

### 3.2. Cấp độ Thực tế (Production-Ready Level)
Mục tiêu là hệ thống có khả năng phục vụ hàng ngàn người dùng, dễ bảo trì, mở rộng và bảo mật tuyệt đối.

| Khía cạnh | Chỉ số Đánh giá (Metrics) | Tiêu chuẩn Đạt |
| :--- | :--- | :--- |
| **Functional** | Code Coverage (SonarQube/Jacoco) | **> 85 - 90%** toàn dự án. Không cho phép merge code nếu Coverage giảm. |
| | Bug / Vulnerability / Code Smell | **0 Bugs, 0 Vulnerabilities**, nợ kỹ thuật (Technical Debt) < 5%. |
| | Automated E2E | Có pipeline CI/CD chạy tự động Automation Test (Selenium/TestFX) trước mỗi lần Release. |
| **Non-functional**| Hiệu suất API (Response Time) | **p95 < 200ms**, **p99 < 500ms** (99% các request hoàn thành dưới 500 mili-giây). |
| | Chịu tải (Throughput) | Hệ thống hỗ trợ xử lý **> 500 TPS** (Transactions Per Second) mà không bị nghẽn Database Pool. |
| | Độ sẵn sàng (Availability/Uptime) | **99.9%** Uptime. Hệ thống có cơ chế Fallback (VD: PayPal sập thì tự động gợi ý VietQR). |
| | Bảo mật (Security Auditing) | Vượt qua các công cụ quét động và tĩnh (DAST/SAST); Không lưu trữ raw password hoặc CVV thẻ. |

---

> [!TIP]
> Việc nâng cấp từ mức **Coursework** lên **Production-Ready** đòi hỏi sự thay đổi lớn về công cụ (tích hợp CI/CD, SonarQube, JMeter cho Load Test, Docker) và tư duy thiết kế (thiết kế chống chịu lỗi - Fault Tolerance). Đối với mục tiêu đồ án hiện tại, việc đạt được các chỉ số Coursework một cách xuất sắc đã là một thành công lớn.
