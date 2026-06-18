# Data Modeling: Non-DBMS Files and Data Architecture Analysis

> **Lưu ý về Kiến trúc Hệ thống:** 
> Dự án AIMS được xây dựng dựa trên mô hình Client-Server hiện đại, sử dụng cơ sở dữ liệu quan hệ **PostgreSQL** để quản lý trạng thái và lưu trữ dữ liệu tập trung. Do đó, hệ thống không sử dụng các khái niệm "file dữ liệu phi CSDL" truyền thống (như ISAM, Sequential Files) để lưu trữ thông tin nghiệp vụ. 
> 
> Tuy nhiên, kiến trúc xử lý dữ liệu của dự án được cấu trúc rõ ràng thông qua các file định nghĩa CSDL (`init-db.sql`), các file cấu hình, và các gói mã nguồn định nghĩa cấu trúc dữ liệu (`package model`) cũng như cơ chế truy xuất (`package repository`). Dưới đây là mô tả chi tiết "ánh xạ" các yêu cầu vào cấu trúc thực tế của dự án.

## 1. Phân loại và Mô tả các File Tham gia Xử lý Dữ liệu

| Tên File / Gói Mã Nguồn | Loại | Mục Đích Sử Dụng | Mức Độ Tạm Thời | Modules Đọc/Ghi |
| :--- | :--- | :--- | :--- | :--- |
| `application.properties` | Input File | Chứa thông tin cấu hình kết nối CSDL (URL, Username, Password, Connection Pool parameters). | Cố định (Permanent) | Đọc bởi: `DatabaseConnection.java` (package `utils`) |
| `init-db.sql` | Input File | Chứa script DDL (Data Definition Language) và DML định nghĩa schema và nạp dữ liệu mẫu ban đầu. | Cố định (Permanent) | Đọc bởi: Quản trị viên hệ thống (chạy qua CLI hoặc GUI tool của PostgreSQL). |
| `package model` <br>*(VD: Product.java, User.java)* | In-memory Object | Biểu diễn cấu trúc bản ghi dữ liệu (Record Structures) trong bộ nhớ khi ứng dụng hoạt động. Là cầu nối giữa CSDL và giao diện. | Tạm thời (Lưu trữ RAM) | Đọc/Ghi bởi: Các class trong `package repository` và `package controller`. |
| `package repository` <br>*(VD: ProductRepository)* | Data Access Component | Chứa logic thực thi truy vấn SQL (CRUD) thông qua JDBC để tương tác với PostgreSQL. | Không áp dụng (Mã nguồn) | Được gọi bởi các `Service` hoặc `Controller`. |
| `App Logs` <br>*(Standard Output/Error)* | Output File | Hệ thống hiện ghi log trực tiếp ra console hoặc file log của môi trường chạy, ghi lại lỗi và trạng thái kết nối pool (HikariCP). | Cố định/Tạm thời tùy cấu hình | Ghi bởi: `DatabaseConnection.java`, Exception handlers. |

## 2. Cấu trúc Bản ghi, Khóa và Phần tử Dữ liệu (Record Structures)

Các cấu trúc bản ghi được ánh xạ trực tiếp từ các thực thể trong `package model` xuống các bảng định nghĩa tại `init-db.sql`.

### 2.1. Cấu trúc Bản ghi: Sản phẩm (Products)
Hệ thống áp dụng mẫu thiết kế **Single Table Inheritance** (Kế thừa đơn bảng), nghĩa là tất cả các loại sản phẩm (Sách, Đĩa CD, DVD) đều dùng chung một bảng `products`.

*   **Khóa chính (Primary Key):** `product_id` (Tự động tăng - SERIAL).
*   **Chỉ mục duy nhất (Unique Index):** Hệ thống đảm bảo tính duy nhất cho phiên bản hiện tại của sản phẩm thông qua biểu thức kết hợp: `(barcode, is_current = true)`.
*   **Các phần tử dữ liệu chung:**
    *   `barcode` (Mã vạch), `title` (Tên), `category` (Danh mục), `original_price` (Giá gốc), `current_price` (Giá hiện tại), `stock` (Tồn kho).
*   **Các phần tử dữ liệu đặc thù (Nullable):**
    *   **Book:** `author`, `publisher`, `pages`, `cover_type`...
    *   **CD:** `artist`, `record_label`, `track_count`...
    *   **DVD:** `director`, `studio`, `disc_type`, `duration`...
*   **Mapping:** Tương ứng với class `Product.java` và các lớp kế thừa `Book.java`, `CD.java`, `DVD.java` trong `package model`.

### 2.2. Cấu trúc Bản ghi: Người dùng (Users)
*   **Khóa chính:** `user_id` (SERIAL).
*   **Khóa phụ (Unique Key):** `username` (Tên đăng nhập).
*   **Các phần tử dữ liệu:** `password` (Chuỗi băm SHA-256), `roles` (Vai trò, cách nhau bởi dấu phẩy), `status` (Trạng thái - ACTIVE/BLOCKED), `full_name`.
*   **Mapping:** Tương ứng với class `User.java` trong `package model`.

### 2.3. Cấu trúc Bản ghi: Đơn hàng (Orders)
*   **Khóa chính:** `order_id` (SERIAL).
*   **Khóa ngoại (Foreign Key):** `user_id` (Tham chiếu bảng `users`).
*   **Các phần tử dữ liệu:** Thông tin giao hàng (`delivery_name`, `address`, `phone`), `subtotal` (Tạm tính), `shipping_fee` (Phí vận chuyển), `total_amount` (Tổng cộng), `order_status` (Trạng thái đơn).

## 3. Chiều dài Bản ghi và Hệ số Khối (Record length & Blocking factors)

*   **Chiều dài bản ghi (Record Length):** Dựa trên hệ quản trị CSDL quan hệ PostgreSQL, các bản ghi đều có **Độ dài biến thiên (Variable Length)**. PostgreSQL tối ưu hóa lưu trữ bằng cách loại bỏ các khoảng trống vô ích. Ví dụ, trường `description` hoặc `delivery_address` kiểu `VARCHAR` hoặc `TEXT` chỉ chiếm dung lượng lưu trữ đúng bằng độ dài của chuỗi ký tự thực tế cộng thêm một vài bytes quản lý. Việc sử dụng Single Table Inheritance khiến bảng `products` có nhiều cột NULL, nhưng PostgreSQL xử lý các cột NULL này với overhead bằng 0.
*   **Hệ số khối (Blocking Factors):** Ở cấp độ vật lý, PostgreSQL tự động nhóm các bản ghi thành các Khối (Blocks/Pages) có kích thước cố định, mặc định là **8KB/page**. Các cấu trúc dữ liệu trên bộ nhớ (`package model`) khi chạy trên JVM chịu sự quản lý bộ nhớ của Garbage Collector và không có khái niệm Blocking Factor rõ ràng.

## 4. Phương thức Truy cập (Access Methods)

Phương thức truy cập vào dữ liệu được thực hiện phân tầng:

*   **Cấp độ Mã nguồn (Data Access Layer - `package repository`):** 
    Truy cập tuần tự ảo (Virtual Sequential) hoặc Truy cập ngẫu nhiên qua Primary Key, thực hiện thông qua **JDBC API**. Ứng dụng kết nối thông qua Connection Pool (quản lý bởi `HikariCP` tại `DatabaseConnection.java`) để tái sử dụng kết nối.
*   **Cấp độ Vật lý (DBMS - xem `init-db.sql`):**
    *   **B-Tree Indexes (Index Sequential Access):** Phương pháp truy xuất chính cho các truy vấn theo khoảng (Range Queries) hoặc so sánh bằng (Equality). 
        *   Ví dụ: `CREATE INDEX idx_products_current_price ON products (current_price);` hỗ trợ lọc theo giá cực nhanh.
    *   **GIN Indexes (Generalized Inverted Index):** Cấu trúc cây đặc biệt (được kích hoạt qua `pg_trgm`) hỗ trợ tìm kiếm mờ (Fuzzy Search/Autocomplete) hiệu quả cao.
        *   Ví dụ: `CREATE INDEX idx_products_title_trgm ON products USING gin (title gin_trgm_ops);` giúp tối ưu hóa truy vấn `LIKE '%từ_khóa%'`.

## 5. Ước tính Dung lượng Dữ liệu (Sizing & Volume Estimates)

Dựa trên kịch bản triển khai thương mại điện tử thực tế của AIMS:

*   **Tập dữ liệu Sản phẩm (`products`):**
    *   Giả sử quản lý **10,000** sản phẩm.
    *   Kích thước trung bình mỗi bản ghi: ~500 bytes.
    *   Dung lượng dữ liệu thô: 10,000 * 500 = **~5 MB**.
    *   Overhead (dành cho B-Tree & GIN Indexes): ~40% dung lượng thô (**~2 MB**).
    *   Tổng cộng file vật lý: **~7 MB**.
*   **Tập dữ liệu Đơn hàng (`orders`, `order_items`):**
    *   Tần suất: 500 đơn hàng / ngày. Trung bình 1 năm: ~182,500 đơn hàng.
    *   Mỗi đơn hàng gồm 1 bản ghi `orders` và trung bình 3 bản ghi `order_items`. Kích thước trung bình một giao dịch: ~1.2 KB.
    *   Dung lượng lưu trữ sau 1 năm: 182,500 * 1.2 KB = **~220 MB**. Thêm overhead Index: **~280 MB / năm**.

## 6. Tần suất Cập nhật Dữ liệu (Update Frequency)

Hệ thống AIMS phục vụ xử lý giao dịch trực tuyến (OLTP). Tần suất cập nhật (Update Frequency) phân bổ không đồng đều:

*   **Tần suất thấp (Low Frequency):** Bảng `users` (chỉ khi đăng ký, đổi pass, block) và `products` (khi Admin nhập kho hoặc chỉnh sửa giá).
*   **Tần suất trung bình (Medium Frequency):** Bảng `stock_change_log` - Ghi nhận lịch sử mỗi khi có hàng hóa ra/vào.
*   **Tần suất cao (High Frequency):**
    *   **Giỏ hàng (`carts`, `cart_items`):** Tần suất cập nhật liên tục mỗi khi user tương tác (thêm/bớt số lượng). Statistical mode: Hàng chục transactions / user session.
    *   **Giao dịch (`orders`, `transactions`):** Phân phối của tập giao dịch này (Transaction Distribution) chủ yếu tập trung vào các "khung giờ vàng" mua sắm (ví dụ: 19h - 22h tối). Ước lượng Peak Time: **50 transactions / minute**.

## 7. Đặc tả Sao lưu và Phục hồi (Backup and Recovery Specifications)

Do toàn bộ dữ liệu nằm trong PostgreSQL:
*   **Backup (Sao lưu):**
    *   Sử dụng công cụ `pg_dump` để sao lưu toàn bộ Schema (cấu trúc) và Data (dữ liệu) thành các file `.sql` định kỳ (Daily Backup).
    *   Lưu giữ các bản backup tại một ổ đĩa riêng biệt hoặc cloud storage (ví dụ AWS S3).
*   **Recovery (Phục hồi):**
    *   Khi có sự cố (ví dụ hỏng ổ đĩa CSDL), thiết lập lại Instance PostgreSQL mới.
    *   Chạy lại `init-db.sql` (nếu cần thiết lập lại Schema rỗng).
    *   Dùng lệnh `psql` hoặc công cụ khôi phục để nạp lại dữ liệu từ file `.sql` backup gần nhất. Tốc độ khôi phục ước tính dưới 15 phút cho dung lượng nhỏ dưới 1GB.
