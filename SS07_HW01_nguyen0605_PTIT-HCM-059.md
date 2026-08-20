# BÁO CÁO PHÂN TÍCH VÀ LỰA CHỌN PHƯƠNG ÁN GIẢI QUYẾT LỖI DIMENSION MISMATCH

## PHẦN 1: TIÊU ĐỀ BÀI TẬP VÀ TÓM TẮT YÊU CẦU
- **Tên bài tập:** BÀI 1: Phân tích & Lựa chọn - Giải quyết lỗi không tương thích số chiều vector (Dimension Mismatch) (Mức độ Khá)
- **Bối cảnh:** Phân hệ CRM Ticket Assistant sử dụng mô hình embedding của OpenAI (`text-embedding-3-small`, 1536 chiều) trên production. Khi phát triển ở môi trường Local (Dev), lập trình viên đổi sang dùng Ollama (`all-minilm`, 384 chiều) để tối ưu chi phí, gây ra lỗi xung đột kiểu dữ liệu: `column "embedding" is of type vector(1536) but expression is of type vector(384)`.
- **Yêu cầu:** Phân tích 3 phương án giải quyết (A, B, C), lựa chọn phương án tối ưu nhất, giải thích chi tiết lý do và chỉ ra nhược điểm của các phương án còn lại dựa trên thiết kế cơ sở dữ liệu, quản lý hạ tầng và tính đồng nhất ngữ nghĩa.

---

## PHẦN 2: GIẢ LẬP CUỘC TRÒ CHUYỆN VỚI AI

### Câu lệnh Prompt gửi AI:
> "Tôi đang gặp lỗi xung đột số chiều vector trong dự án CRM Ticket Assistant khi chạy Spring Boot kết nối PostgreSQL (pgvector) ở môi trường local. Ở Prod dùng mô hình OpenAI 1536 chiều, còn Local dev định dùng Ollama 384 chiều. Có 3 phương án giải quyết: (A) Sửa DDL cục bộ rồi chạy script thay đổi kiểu dữ liệu khi deploy; (B) Tách hai bảng khác nhau trong db (vector_store_local và vector_store_prod) cấu hình qua dynamic table-name; (C) Giữ nguyên schema 1536 chiều ở cả hai môi trường và dùng model hỗ trợ 1536 chiều hoặc API Key test ở local. Hãy giúp tôi phân tích chi tiết, lựa chọn phương án tối ưu nhất và viết mã nguồn Spring Boot minh họa hoàn chỉnh."

### Tóm tắt phản hồi của AI:
1. **Phân tích lựa chọn:** AI đã nhanh chóng chỉ ra **Phương án C** là tối ưu nhất vì nó tuân thủ triệt để nguyên lý nhất quán môi trường (Environment Parity) trong kiến trúc 12-Factor App, giúp bảo vệ toàn vẹn dữ liệu và đảm bảo việc kiểm thử nghiệp vụ tìm kiếm tương đồng (Semantic Search) diễn ra chính xác.
2. **Đưa ra giải pháp cài đặt:** Cung cấp mã nguồn cấu hình đa môi trường (Spring Boot Profiles) cho phép chuyển đổi linh hoạt giữa Ollama (sử dụng model có số chiều tương đương hoặc kỹ thuật pad) và OpenAI mà không thay đổi cấu trúc bảng cơ sở dữ liệu.

---

## KHUNG LÀM BÀI CỦA SINH VIÊN

### Đáp án lựa chọn: Phương án C
Giữ nguyên cấu hình bảng `vector_store` với số chiều 1536 ở cả hai môi trường. Trên môi trường Local, thay vì dùng model `all-minilm` (384 chiều), sử dụng mô hình embedding chạy local khác hỗ trợ output 1536 chiều hoặc giữ nguyên cấu hình OpenAI sử dụng API Key dùng thử/mạng nội bộ được cấp riêng cho Dev.

### Giải thích lý do lựa chọn:
1. **Tính nhất quán của Cơ sở dữ liệu (Database Schema Parity):**
   - Quy tắc tối kỵ trong kỹ thuật phần mềm là duy trì các phiên bản schema cơ sở dữ liệu khác nhau giữa Dev và Prod. Việc giữ nguyên số chiều `1536` ở mọi môi trường giúp đảm bảo mã nguồn migration (như Flyway, Liquibase) chạy đồng nhất, giảm thiểu tối đa rủi ro "lệch pha" schema (schema drift) khi triển khai thực tế.
2. **Tính chính xác và Đồng nhất của Không gian Vector (Vector Space Consistency):**
   - Không gian vector là một không gian toán học biểu thị ngữ nghĩa. Ta không thể so sánh hoặc chạy kiểm thử tích hợp (Integration Tests) đáng tin cậy nếu không gian biểu diễn ngữ nghĩa ở local hoàn toàn khác biệt so với production. Việc giữ nguyên 1536 chiều (bằng cách dùng các mô hình local tương thích như `nomic-embed-text` cấu hình dimensionality phù hợp, hoặc sử dụng proxy/API Key Dev) đảm bảo logic tính toán khoảng cách Cosine hoạt động giống hệt nhau ở cả hai môi trường.
3. **Giảm thiểu độ phức tạp của Code base:**
   - Không cần viết thêm logic xử lý dynamic table-name phức tạp trong mã nguồn Java. Toàn bộ cấu trúc thực thể (Entity) và tầng Repository được giữ nguyên bản, dễ đọc, dễ bảo trì.

### Phân tích các phương án loại trừ:

#### Nhược điểm của Phương án A:
- **Rủi ro cực lớn khi Migration:** Việc thực thi câu lệnh SQL để thay đổi kiểu dữ liệu cột từ `vector(384)` sang `vector(1536)` khi deploy lên Prod là thao tác cực kỳ nguy hiểm. Đối với bảng dữ liệu lớn (hàng trăm ngàn bản ghi), việc `ALTER COLUMN TYPE` sẽ khóa bảng (Table Lock), gây gián đoạn dịch vụ (downtime) nghiêm trọng và tốn tài nguyên tính toán để rebuild lại index (như HNSW index).
- **Khả năng mất mát dữ liệu:** Việc thay đổi số chiều đột ngột dễ dẫn đến lỗi không tương thích, mất mát hoặc sai lệch dữ liệu vector hiện tại của khách hàng.

#### Nhược điểm của Phương án B:
- **Phá vỡ cấu trúc cơ sở dữ liệu sạch:** Việc duy trì song song hai bảng `vector_store_local` và `vector_store_prod` trong cơ sở dữ liệu làm phình to schema không cần thiết, gây khó khăn cho việc quản lý mã nguồn SQL migration.
- **Kiểm thử bất khả thi:** Kết quả tìm kiếm tương đồng trên bảng local (384 chiều) sẽ hoàn toàn khác biệt với bảng prod (1536 chiều). Điều này khiến quy trình kiểm thử nghiệp vụ tìm kiếm nội dung tương đồng (Semantic Search) ở local mất đi độ tin cậy thực tế (false confidence).