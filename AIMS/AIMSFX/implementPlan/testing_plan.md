# Kế hoạch Xây dựng Unit Test Toàn diện (Blackbox & Whitebox)

Mục tiêu của kế hoạch này là cung cấp một chiến lược kiểm thử chuyên sâu cho dự án `AIMS_ITSS20252_Coursework`. Kế hoạch không chỉ đáp ứng yêu cầu khắt khe của một đồ án môn học (Coursework) mà còn tiệm cận với các tiêu chuẩn thực tế trong môi trường Production (như độ phủ mã - Code Coverage, Mocking, và CI/CD readiness).

Dựa trên việc tham khảo `PlaceOrderServiceTest.java` hiện có, dự án đã có nền tảng tốt về JUnit 5 và Mockito. Chúng ta sẽ mở rộng và hệ thống hóa điều này.

---

## 1. Phương pháp Kiểm thử (Testing Methodology)

### 1.1 Blackbox Testing (Kiểm thử Hộp đen)
Tập trung vào đầu vào (Input) và đầu ra (Output) mà không quan tâm đến logic code bên trong. 
- **Kỹ thuật áp dụng:**
  - *Equivalence Partitioning (Phân vùng tương đương):* Áp dụng cho các hàm Validate (Ví dụ: Email hợp lệ vs Email không hợp lệ).
  - *Boundary Value Analysis (Phân tích giá trị biên):* Cực kỳ quan trọng cho logic tính phí. Ví dụ: Tính phí ship cho giỏ hàng có `subtotal = 99.999đ`, `100.000đ`, `100.001đ`; hoặc cân nặng `3.0kg` và `3.1kg`.
  - *State Transition Testing:* Kiểm thử sự chuyển đổi trạng thái của đơn hàng (`PENDING` -> `PROCESSING` -> `COMPLETED` / `FAILED`).

### 1.2 Whitebox Testing (Kiểm thử Hộp trắng)
Tập trung vào cấu trúc code, các nhánh điều kiện (if/else), vòng lặp và xử lý ngoại lệ (Exception).
- **Kỹ thuật áp dụng:**
  - *Statement Coverage (Độ phủ lệnh):* Đảm bảo mọi dòng code đều được chạy qua ít nhất 1 lần.
  - *Branch/Decision Coverage (Độ phủ nhánh):* Đảm bảo mọi mệnh đề `if`, `catch` đều được rẽ vào. Ví dụ: Phải viết test case để code nhảy vào nhánh `catch (SQLException e)` và thực hiện `conn.rollback()` trong `OrderRepository`.
  - *Path Coverage:* Kiểm tra luồng đi kết hợp. Ví dụ trong `PaymentService`: Luồng "Trừ tồn kho thành công -> Gọi PayPal lỗi timeout -> Log PENDING".

---

## 2. Kế hoạch Chi tiết theo Cục bộ (Local Scope - Layer by Layer)

### Phase 1: Models & Utils Layer (Độ khó: Thấp, Coverage kỳ vọng: 100%)
- **Mục tiêu:** Đảm bảo các đối tượng dữ liệu và công cụ dùng chung không có lỗi tiềm ẩn.
- **Blackbox:** Test các hàm getter/setter, constructor của Model.
- **Whitebox:** 
  - Test `DatabaseConnection.java`: Singleton pattern có hoạt động đúng khi gọi ở nhiều Thread? (Sử dụng Multi-threading test).
  - Test việc đọc file `application.properties` (Giả lập file bị thiếu để test nhánh Exception).

### Phase 2: Business Logic / Service Layer (Độ khó: Trung bình, Coverage kỳ vọng: >90%)
*(Đây là nơi quan trọng nhất của Unit Test)*
- **Mục tiêu:** Kiểm chứng các quy tắc nghiệp vụ. Lớp `PlaceOrderServiceTest` đã làm khá tốt, cần nhân rộng ra `PaymentService`, `CartService`, `OrderReviewService`.
- **Blackbox:**
  - `PaymentService`: Input là Order và paymentMethod -> Output là trạng thái thanh toán.
- **Whitebox:**
  - *Saga Pattern Flow:* Sử dụng Mockito để mock `ProductRepository` trả về Exception khi trừ tồn kho (`deductStockForOrder()`), kiểm tra xem hệ thống có bắt lỗi và dừng việc gọi external API hay không.
  - *CronJob Logic:* Inject một `Clock` hoặc mock thời gian để kiểm tra logic quét các transaction `PENDING` bị kẹt.

### Phase 3: Data Access / Repository Layer (Độ khó: Cao, Coverage kỳ vọng: >85%)
- **Mục tiêu:** Đảm bảo các câu lệnh SQL và Transaction hoạt động chuẩn xác.
- **Whitebox:**
  - Test logic Atomicity vừa sửa ở `OrderRepository.java`. Mock `PreparedStatement` ném ra `SQLException` ở bước `saveOrderItems` để verify rằng phương thức `conn.rollback()` *thực sự được gọi*.
  - Kiểm tra logic Optimistic Locking (`WHERE order_id = ? AND order_status = ?`) trong việc chống Race Condition.
- **Tiêu chuẩn Production (Advanced):** Thay vì mock JDBC (rất phức tạp và không phản ánh đúng SQL), nên sử dụng **In-Memory Database (H2)** hoặc **Testcontainers (Docker PostgreSQL)** để chạy Unit Test cho Repository. Đây là tiêu chuẩn vàng trong thực tế.

### Phase 4: Controller / API Layer (Độ khó: Trung bình, Coverage kỳ vọng: 80%)
- **Mục tiêu:** Kiểm tra định tuyến (Routing), Parsing Request, và Format Response.
- **Whitebox & Blackbox kết hợp:** 
  - Test việc trả về mã HTTP 200 OK, 400 Bad Request, 500 Internal Server Error dựa trên các Exception ném ra từ Service.

---

## 3. Kiểm thử Tổng thể Toàn dự án (Global Scope & Integration)

Ở góc nhìn khái quát của một kỹ sư chất lượng phần mềm (QA/QC) và kỹ sư phát triển:
1. **Mocking External APIs (WireMock):** Dự án giao tiếp với VietQR và PayPal. Ở production, người ta không bao giờ call API thật trong Unit Test. Cần dùng thư viện giả lập HTTP response (thành công, timeout, HTTP 500) để test xem hệ thống phản ứng ra sao.
2. **Báo cáo Code Coverage (Jacoco):** Tích hợp plugin Jacoco vào `pom.xml`. Đặt ra "Quality Gate": Nếu Code Coverage tổng thể của dự án dưới 80%, quá trình build sẽ thất bại (Fail Build). Đây là tư duy CI/CD chuyên nghiệp.
3. **Idempotency Testing:** Đảm bảo hệ thống an toàn khi bị gọi API liên tục. Test việc gửi request thanh toán 2 lần liên tiếp cùng một `transactionId`, hệ thống chỉ xử lý 1 lần.

---

## 4. Công cụ và Thư viện đề xuất bổ sung vào `pom.xml`

- `junit-jupiter-api` & `junit-jupiter-engine` (Đã có)
- `mockito-core` & `mockito-junit-jupiter` (Đã có)
- **`assertj-core`**: (Mới) Giúp viết câu lệnh assert cực kỳ tự nhiên kiểu: `assertThat(order.getStatus()).isEqualTo("PENDING");` thay vì `assertEquals`.
- **`h2`** hoặc **`testcontainers`**: (Mới) Phục vụ test Database thực tế.
- **`jacoco-maven-plugin`**: (Mới) Tạo báo cáo coverage trực quan (HTML file).

---

## User Review Required

> [!IMPORTANT]
> Bản kế hoạch trên đã phân tích chi tiết từ góc độ hộp đen (Blackbox) đến hộp trắng (Whitebox) cho cả mức cục bộ và tổng thể.
> 
> **Câu hỏi dành cho Anh/Chị:**
> Anh/Chị có muốn em tiến hành **thực thi** bản kế hoạch này bằng cách:
> 1. Setup thư viện (Jacoco, AssertJ) vào `pom.xml`?
> 2. Viết ngay một file Unit Test nâng cao (ví dụ: `PaymentServiceTest.java` hoặc `OrderRepositoryTest.java`) để làm mẫu cho chiến lược Whitebox xử lý Transaction/Rollback không ạ?
