# Phân tích Thiết kế Mẫu (Design Patterns) trong Hệ thống AIMS

Tài liệu này cung cấp một cái nhìn toàn diện và chuyên sâu về các Design Pattern hiện đang được ứng dụng trong dự án AIMS, cũng như các đề xuất mở rộng trong tương lai dựa trên cấu trúc mã nguồn hiện tại.

---

## 1. Các Design Pattern Đang Được Ứng Dụng

Sau quá trình rà soát source code, hệ thống hiện đang triển khai rất tốt các nguyên tắc thiết kế phần mềm (SOLID) và ứng dụng linh hoạt nhiều GoF Design Patterns.

### 1.1. Singleton Pattern (Nhóm Creational)
- **Định nghĩa lý thuyết**: Đảm bảo một lớp (class) chỉ có duy nhất một thể hiện (instance) trong suốt vòng đời của ứng dụng và cung cấp một điểm truy cập toàn cầu tới thể hiện đó.
- **Nơi ứng dụng**: 
  - `DatabaseConnection.java`: Quản lý pool kết nối cơ sở dữ liệu HikariCP.
  - `SessionManager.java`: Quản lý phiên làm việc của người dùng.
- **Lý do sử dụng**:
  - **Quản lý tài nguyên chung**: Kết nối cơ sở dữ liệu (Database Connection Pool) là một tài nguyên đắt đỏ và có giới hạn. Nếu mỗi luồng hoặc yêu cầu đều tạo một pool mới, hệ thống sẽ sập do quá tải số lượng kết nối (connection leak).
  - **Trạng thái toàn cục**: Quản lý phiên làm việc yêu cầu tính nhất quán trên toàn bộ ứng dụng tại một thời điểm nhất định.
- **Quy trình & Kết quả ứng dụng**:
  - Được triển khai thông qua *Private Constructor* để chặn việc khởi tạo từ bên ngoài và *Double-Checked Locking* (`synchronized` block) khi gọi `getInstance()` nhằm đảm bảo Thread-safe.
  - **Kết quả**: Tài nguyên hệ thống được tối ưu; tránh được lỗi rò rỉ kết nối; việc truy cập CSDL từ các Controller/Repository dễ dàng và đồng nhất thông qua `DatabaseConnection.getInstance().getConnection()`.
- **So sánh (Có vs. Không có)**:
  - *Nếu không dùng*: Mã nguồn sẽ liên tục khởi tạo cấu hình DB `new HikariDataSource()`, làm lãng phí RAM, thời gian khởi tạo chậm (giảm hiệu năng rõ rệt) và khả năng sập DB cao (chi phí vận hành lớn).
  - *Khi dùng*: Tính khả thi cao, thời gian đáp ứng CSDL siêu tốc nhờ duy trì Pool sẵn có.

### 1.2. Strategy Pattern (Nhóm Behavioral)
- **Định nghĩa lý thuyết**: Định nghĩa một tập hợp các thuật toán/phương pháp, đóng gói từng thuật toán lại và làm cho chúng có thể thay thế lẫn nhau. Strategy giúp thuật toán có thể thay đổi độc lập so với client sử dụng nó.
- **Nơi ứng dụng**: 
  - Giao diện thanh toán: `IPaymentMethodHandler` (với các implementation như `PayPalPaymentHandler`, `VietQRPaymentHandler`).
  - Tính phí giao hàng: `IDeliveryFeeCalculator` (với `WeightBasedFeeCalculator`).
- **Lý do sử dụng**:
  - Tách biệt UI (hoặc logic tính toán) khỏi các quy tắc nghiệp vụ/phương thức thanh toán cụ thể. 
  - Tuân thủ triệt để OCP (Open/Closed Principle): Khi cần thêm phương thức giao hàng mới (như hỏa tốc) hay thanh toán mới (như Momo, ZaloPay), hệ thống không cần sửa code cũ.
- **Quy trình & Kết quả ứng dụng**:
  - **PaymentUI**: Cấu trúc UI sở hữu một Map/Dictionary chứa các `IPaymentMethodHandler`. Khi người dùng click Radio Button, UI chỉ việc thay đổi `activeHandler` và gọi `activeHandler.setupView()`, `activeHandler.handlePayment()`.
  - **Kết quả**: `PaymentUI.java` không chứa bất kỳ logic `if-else` hay `switch-case` nào về thanh toán. Số lượng dòng code được kiểm soát tốt, việc bảo trì hay thay thế cực kỳ dễ dàng.
- **So sánh (Có vs. Không có)**:
  - *Nếu không dùng*: Bắt buộc phải dùng chuỗi `if(method == "vietqr") {...} else if(method == "paypal") {...}`. UI bị "phình to" và Coupling cao (dính líu tới thư viện SDK bên ngoài).
  - *Khi dùng*: Đánh đổi bằng việc số lượng class/file tăng lên. Tuy nhiên hiệu suất về lâu dài, đặc biệt thời gian bảo trì (maintenance cost) giảm đi rất nhiều. Tính khả thi để mở rộng là 100%.

### 1.3. Factory Method / Abstract Factory Pattern (Nhóm Creational)
- **Định nghĩa lý thuyết**: Định nghĩa một interface/lớp trừu tượng để tạo ra đối tượng, nhưng để các lớp con quyết định lớp nào sẽ được khởi tạo. 
- **Nơi ứng dụng**: 
  - Quy trình tạo sản phẩm: `ProductFactory`, `PhysicalProductFactory` cùng với các lớp con `BookFactory`, `CDFactory`, `DVDFactory`, v.v.
- **Lý do sử dụng**:
  - Ẩn giấu sự phức tạp của quá trình khởi tạo các đối tượng Product khác nhau.
  - Mỗi sản phẩm (Book, CD, DVD) có các thuộc tính và cách xác thực khác nhau. Việc để một hàm khởi tạo chung cho tất cả là không thể và vi phạm SRP (Single Responsibility Principle).
- **Quy trình & Kết quả ứng dụng**:
  - Giao diện `ProductFactory` định nghĩa một hàm `createProduct(...)` chung.
  - Các lớp `BookFactory`, `CDFactory` hiện thực hàm này để lấy đúng các tham số `attributes` (được truyền theo varargs hoặc map) và trả về `Book`, `CD`.
  - **Kết quả**: Khi `ProductService` hoặc `Repository` cần tạo sản phẩm, nó chỉ việc gọi Factory tương ứng thông qua enum `ProductType`.
- **So sánh (Có vs. Không có)**:
  - *Nếu không dùng*: Class `ProductService` sẽ đầy rẫy `if(type == BOOK) return new Book(...)`. Gây khó khăn lớn khi team nghiệp vụ muốn thêm sản phẩm "Newspaper" hay "DigitalBook".
  - *Khi dùng*: Đem lại tính đóng gói hoàn hảo.

### 1.4. Adapter / Facade Pattern (Nhóm Structural)
- **Định nghĩa lý thuyết**: 
  - *Adapter*: Cho phép các interface không tương thích hoạt động cùng nhau (Wrappers).
  - *Facade*: Cung cấp một giao diện đơn giản, đồng nhất cho một hệ thống con (subsystem) phức tạp.
- **Nơi ứng dụng**: 
  - Gói `com.aimsfx.subsystem.paypal.PayPalSubsystem` (implements `IPaymentGateway`).
- **Lý do sử dụng**:
  - SDK của bên thứ ba (như PayPal SDK) thường rất phức tạp (bao gồm `OrdersController`, `PurchaseUnitRequest`, `AmountWithBreakdown`...).
  - Application của AIMS chỉ quan tâm đến 2 hành động: "Tạo đơn hàng (Create Order)" và "Chốt thanh toán (Capture Order)".
- **Quy trình & Kết quả ứng dụng**:
  - `PayPalSubsystem` đóng vai trò là một Facade/Adapter, bao bọc mọi sự phức tạp của Paypal SDK. Nó ánh xạ các hàm phức tạp đó ra một giao diện duy nhất `IPaymentGateway` mà ứng dụng AIMS hiểu được. 
  - Nó còn ánh xạ các exception riêng của Paypal thành `PaymentException` chung của hệ thống.
  - **Kết quả**: Tách biệt hoàn toàn AIMS khỏi sự phụ thuộc vòng đời của PayPal SDK.
- **So sánh (Có vs. Không có)**:
  - *Nếu không dùng*: Cấu trúc Paypal SDK bị lộ ra cho các tầng `Controller` và `Service`. Khi Paypal đổi phiên bản SDK (ví dụ từ V1 lên V2), toàn bộ ứng dụng bị vỡ (broken).
  - *Khi dùng*: Chi phí thay đổi (change cost) bằng 0 đối với Core Logic. Chỉ cần cập nhật duy nhất `PayPalSubsystem`.

### 1.5. Registry Pattern (Được sử dụng như một Map Factory)
- **Nơi ứng dụng**: `ProductFactoryRegistry.java`, `ValidatorRegistry.java`.
- **Đánh giá**: Hoạt động như bộ máy tra cứu trung tâm để lấy các Factory hoặc Validator ứng với loại sản phẩm O(1), thay vì phải dò tìm thủ công.

---

## 2. Đề xuất ứng dụng Design Pattern trong tương lai

Dù kiến trúc hiện tại khá tốt, nhưng khi mở rộng (Scale-up) nghiệp vụ, có một số mẫu GoF mang lại lợi thế lớn:

### 2.1. Observer Pattern (Nhóm Behavioral)
- **Định nghĩa**: Xác lập mối quan hệ một-nhiều giữa các đối tượng để khi một đối tượng thay đổi trạng thái, tất cả các đối tượng phụ thuộc đều được tự động thông báo và cập nhật.
- **Khả năng ứng dụng**:
  - **Giỏ hàng (Cart)**: Khi có sự thay đổi trong `Cart` (Thêm/Sửa/Xóa sản phẩm, thay đổi số lượng), `CartView` (UI giỏ hàng), `MenuIcon` (Icon số lượng trên góc màn hình), và `PromotionService` (Hệ thống tính khuyến mãi tự động) cần được cập nhật.
  - Trạng thái mã nguồn hiện tại đang gọi cập nhật thủ công. Có thể áp dụng Observer: `Cart` đóng vai trò là *Subject*, các thành phần View/Service là *Observer*.
- **Tính khả thi**: Rất cao. Chỉ cần định nghĩa interface `CartObserver` với hàm `onCartUpdated(Cart cart)`.
- **Đánh đổi**: Luồng thực thi (control flow) không còn theo thứ tự tuần tự rõ ràng từ trên xuống dưới, đôi khi khó debug hơn nếu có quá nhiều Observer lắng nghe. Hiệu năng bị ảnh hưởng chút ít do phải lặp qua danh sách listener mỗi khi có sự thay đổi, nhưng không đáng kể đối với bài toán UI.

### 2.2. Template Method Pattern (Nhóm Behavioral)
- **Định nghĩa**: Định nghĩa bộ khung (skeleton) của một thuật toán trong một phương thức (method), trì hoãn việc định nghĩa một vài bước xuống các lớp con (subclasses).
- **Khả năng ứng dụng**: 
  - Quá trình xác thực (`Validator`): Hiện tại có `CommonProductValidator` đóng vai trò helper. Ta có thể tái cấu trúc thành Template Method trong class `AbstractProductValidator` với phương thức `final validate(ProductDTO data)`:
    ```java
    public final void validate(ProductDTO data) {
        validateCommonFields(data); // Cố định
        validateSpecificFields(data); // Subclasses thực thi
    }
    ```
  - Quá trình Thanh toán (Checkout Flow): Xác thực tồn kho -> Khóa kho -> Gọi Subsystem thanh toán -> Trừ kho thực tế -> Cập nhật hóa đơn.
- **Tính khả thi**: Cao, phù hợp để tạo luồng chuẩn hóa.
- **Đánh đổi**: Có sự dính chặt (Coupling) về cấu trúc phân cấp kế thừa (Inheritance). Subclass bị gò bó vào bộ khung của Superclass.

### 2.3. Decorator Pattern (Nhóm Structural)
- **Định nghĩa**: Cung cấp cách gán các trách nhiệm (trạng thái/hành vi) bổ sung cho một đối tượng tại thời điểm chạy (runtime) thay vì dùng tính kế thừa (inheritance).
- **Khả năng ứng dụng**:
  - **Hệ thống tính tiền và Khuyến mãi (Pricing & Discount)**: Giả sử tương lai AIMS có "Voucher giảm 10%", "Freeship cho đơn trên 1 triệu", "Giảm 5% cho KH thân thiết".
  - Một hóa đơn (`Invoice`) có thể được bọc (wrap) qua nhiều lớp Decorator:
    - `BaseInvoice` -> `VIPDiscountDecorator` -> `ShippingFeeDecorator`.
    - Mỗi decorator sẽ lấy giá trị của đối tượng bên trong nó, tự tính toán và cộng dồn/trừ đi.
- **Tính khả thi**: Khả thi và là chuẩn mực tốt nhất cho các hệ thống thương mại điện tử (E-commerce) để áp dụng chính sách giá chồng chéo mà không làm rách (violate) OCP.
- **Đánh đổi**: Tạo ra quá nhiều lớp nhỏ lẻ (small classes) có vẻ ngoài giống nhau, có thể gây khó hiểu cho người mới tiếp cận codebase. Quá trình debug (chain of method calls) có thể sâu.

### 2.4. Command Pattern (Nhóm Behavioral)
- **Định nghĩa**: Đóng gói một yêu cầu dưới dạng một đối tượng, nhờ đó bạn có thể tham số hóa các client với các yêu cầu khác nhau, queue/log request, và hỗ trợ tính năng undo (hoàn tác).
- **Khả năng ứng dụng**:
  - Cung cấp tính năng **Undo/Redo** cho người dùng khi quản lý Giỏ hàng (Undo việc xóa nhầm sản phẩm) hoặc khi nhân viên Admin sửa thông tin Sản phẩm.
- **Tính khả thi**: Trung bình, đòi hỏi phải tái thiết kế kiến trúc hành động trên View.
- **Đánh đổi**: Chi phí thiết kế ban đầu lớn (thời gian), phải tạo ra class riêng cho từng action (`AddToCartCommand`, `RemoveFromCartCommand`). Bù lại, hệ thống cực kỳ an toàn và chuyên nghiệp.

---
**Tổng kết:** Source code hiện tại đang đạt được chuẩn mực thiết kế OOP rất tốt thông qua Singleton, Strategy, Factory Method, và Facade. Mọi thay đổi hoặc bổ sung theo GoF Pattern (như Observer hay Decorator) đều hoàn toàn có khả năng cấy ghép trơn tru lên nền tảng kiến trúc linh hoạt sẵn có này.
