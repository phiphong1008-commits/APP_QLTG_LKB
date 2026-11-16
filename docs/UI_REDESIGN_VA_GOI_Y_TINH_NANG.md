# Thiết kế lại giao diện & gợi ý tính năng

> Nhánh: `feature/ui-redesign`  
> Ứng dụng: APP_QLTG - Quản lý thời gian, công việc và nhật ký (Android + Firebase)

---

## 1. Những gì đã thay đổi trong giao diện

### Phong cách tổng thể
- **Bảng màu mới**: xanh indigo `#4C6FFF` làm màu chính, gradient tím-xanh cho các thẻ nổi bật, nền xám dịu `#F6F7FB`, màu trạng thái rõ ràng (đỏ/cam/xanh).
- **Bo góc lớn (16–24dp)**, đổ bóng nhẹ, viền mảnh thay cho các khối màu đậm.
- **Phân cấp chữ rõ ràng**: tiêu đề 26sp, tiêu đề mục 15–18sp, nội dung 13–16sp, phụ chú 12–13sp.
- **Khoảng trắng thoáng hơn**, mỗi màn hình có tiêu đề + dòng phụ đề thân thiện.

### Từng màn hình
| Màn hình | Thay đổi chính |
| --- | --- |
| Điều hướng chính | Bottom nav trắng, icon + nhãn, chỉ báo tab đang chọn dạng viên thuốc, có đường phân cách |
| Lịch | Tiêu đề "Lịch trình", nút "Hôm nay", lịch trong thẻ bo tròn, nhãn ngày đang chọn, số việc trong ngày, trạng thái trống, FAB thêm việc đúng ngày đang chọn |
| Công việc | Lời chào ngày, **thẻ tiến độ gradient** (x/y việc + thanh tiến độ), **bộ lọc Tất cả / Chưa xong / Đã xong**, trạng thái trống theo ngữ cảnh, FAB |
| Nhật ký | Banner "Hôm nay có gì mới?", danh sách dạng thẻ có ô ngày, trạng thái trống, FAB |
| Tài khoản | Thẻ hero gradient với avatar chữ cái, tên/email, 3 chỉ số thống kê, các nút tiện ích dạng danh sách thay vì nút lớn |
| Đăng nhập / Đăng ký | Header gradient, logo, biểu mẫu Material Outlined có icon, nút đăng nhập nổi bật, liên kết chuyển màn hình |
| Thêm / sửa công việc | Chia 2 nhóm "Thông tin công việc" và "Thời gian & ưu tiên", chọn icon có nhãn, chọn ưu tiên dạng chip màu, nút lưu gradient |
| Viết nhật ký | Ngày dạng viên thuốc, tiêu đề + nội dung viết liền mạch, ghi chú riêng tư, nút lưu |
| Danh sách công việc | Thẻ có thanh màu ưu tiên, icon loại việc, giờ, nhãn ưu tiên, mô tả ngắn, checkbox; xong việc thì mờ + gạch ngang |
| Danh sách nhật ký | Ô ngày nổi bật, tiêu đề, trích đoạn 2 dòng, mũi tên mở chi tiết |

### Kỹ thuật
- Xử lý **edge-to-edge + window insets** để nội dung không bị thanh trạng thái/thanh điều hướng che.
- Chuyển tab có hiệu ứng fade nhẹ và tự dọn màn hình nhập liệu đang mở.
- Bỏ đoạn ghi `"Hello, World!"` vào Firebase mỗi lần mở app.
- Giữ nguyên toàn bộ ID view và logic Firebase hiện có.
- Build kiểm tra thành công: `gradlew.bat assembleDebug`.

### File/bố cục mới
- `fragment_diary_list.xml` (tách danh sách nhật ký khỏi layout công việc).
- Bộ icon vector mới: `ic_add`, `ic_calendar`, `ic_clock`, `ic_check_circle`, `ic_chevron_right`, `ic_lock`, `ic_mail`, `ic_logout`, `ic_note`, `ic_sparkle`, `ic_flag`, `ic_delete`, `ic_arrow_back`.
- Bộ nền/thẻ: `bg_gradient_primary`, `bg_gradient_header`, `bg_pill_*`, `bg_icon_tile`, `bg_empty_icon`, `selector_priority_chip`...
- Bộ màu trạng thái: `res/color/*` (chip, icon, checkbox, bottom nav).

---

## 2. Ứng dụng đã tham khảo

| Ứng dụng | Điểm học hỏi | Áp dụng |
| --- | --- | --- |
| **TickTick** | Lịch + công việc + thói quen trong một app; danh sách sạch, tập trung hôm nay | Thẻ tiến độ, bộ lọc, bố cục lịch gọn |
| **Todoist** | Nhập việc nhanh, ưu tiên P1–P4, bộ lọc linh hoạt | Ưu tiên dạng chip màu, bộ lọc trạng thái |
| **Microsoft To Do** | "My Day", giao diện thân thiện, cá nhân hóa | Lời chào ngày, trạng thái trống có hướng dẫn, avatar |
| **Google Tasks** | Tối giản, thêm việc đúng ngày, ít bước | FAB thêm việc, lịch chọn ngày |
| **Things 3** | Nhiều khoảng trắng, chi tiết tinh tế, cảm giác nhẹ nhàng | Bo góc, typography, microcopy |
| **Day One** | Nhật ký dạng thẻ, gợi ý viết, nhấn mạnh riêng tư | Banner viết nhật ký, ghi chú "chỉ bạn xem được" |
| **Notion Calendar / Google Calendar** | Tập trung vào ngày đang chọn, agenda rõ ràng | Nhãn ngày, danh sách việc theo ngày |

Tham khảo thêm:
- https://ticktick.com
- https://www.todoist.com
- https://www.microsoft.com/en-us/microsoft-365/microsoft-to-do-list-app
- https://tasks.google.com
- https://culturedcode.com/things/
- https://dayoneapp.com

---

## 3. Danh sách tính năng có thể bổ sung

### P0 — Nên làm tiếp theo (giá trị cao, hợp kiến trúc hiện tại)
1. **Công việc lặp lại** (hằng ngày / tuần / tháng, các ngày trong tuần).
   - Thêm `recurrence` vào `Task`, tạo task kế tiếp khi hoàn thành hoặc dùng Cloud Function.
   - Tham khảo: TickTick, Todoist.
2. **Nhiều lời nhắc cho một việc** (trước 5 phút, 1 giờ, 1 ngày) + nhắc theo giờ.
   - Mở rộng `NotificationReceiver` + `AlarmManager`, lưu mảng `reminders`.
3. **Tìm kiếm & lọc nâng cao** (theo từ khóa, khoảng ngày, ưu tiên, trạng thái, loại việc).
   - Thêm SearchView + chip lọc; có thể lọc cục bộ trước, sau đó dùng Firebase Query.
4. **Danh sách / dự án / nhãn** (Học tập, Công việc, Cá nhân…).
   - Thêm node `Lists` và `listId` trong `Task`; màn hình chọn danh sách.
   - Tham khảo: Todoist projects, Microsoft To Do lists.
5. **Việc con (subtasks)** và tiến độ `2/5`.
   - Node `Subtasks` hoặc mảng lồng trong `Task`; hiển thị thanh tiến độ trong thẻ.
6. **Chỉnh sửa hồ sơ**: đổi tên hiển thị, ảnh đại diện, đổi mật khẩu.
   - `FirebaseUser.updateProfile`, Firebase Storage cho ảnh.
7. **Chế độ tối (Dark mode)** và tùy chọn cỡ chữ.
   - Hiện app đang cố định giao diện sáng; có thể bổ sung `values-night/colors.xml` đầy đủ.
8. **Lưu offline & đồng bộ khi có mạng**.
   - `FirebaseDatabase.getInstance().setPersistenceEnabled(true)` + trạng thái đồng bộ.
9. **Widget màn hình chính** (hôm nay / thêm việc nhanh).
   - Glance (Compose) hoặc RemoteViews.
10. **Xuất / sao lưu dữ liệu** (JSON, CSV, Markdown cho nhật ký).

### P1 — Nâng cấp trải nghiệm
11. **Chế độ xem Tuần / Agenda** cho lịch, kéo-thả đổi ngày.
    - Tham khảo: TickTick, Notion Calendar.
12. **Quick Add bằng ngôn ngữ tự nhiên**: "Họp nhóm 9h mai #work !cao".
    - Parse chuỗi + preview trước khi lưu.
13. **Pomodoro / chế độ tập trung** gắn với công việc.
    - TickTick, Forest.
14. **Thói quen (habit tracker)** và streak.
    - Thêm node `Habits`, `HabitLogs`; biểu đồ 7 ngày.
15. **Thống kê & biểu đồ**: việc hoàn thành theo tuần, tỉ lệ đúng hạn, thời gian tập trung.
    - MPAndroidChart hoặc biểu đồ tự vẽ.
16. **Nhật ký nâng cao**: cảm xúc (mood), ảnh, thời tiết, prompt gợi ý mỗi ngày.
    - Day One, Journey.
17. **Khóa nhật ký bằng PIN / vân tay**.
    - Android BiometricPrompt.
18. **Tìm kiếm nhật ký, ghim bài, lịch sử theo tháng**.
19. **Nhắc theo vị trí** (đến trường/công ty thì nhắc việc).
    - Geofencing API.
20. **Chia sẻ danh sách / cộng tác**.
    - Thêm `members`, quyền truy cập trên Firebase Rules.

### P2 — Khác biệt hóa
21. **Trợ lý AI**: gợi ý lịch trình trong ngày, tóm tắt nhật ký, chia nhỏ việc lớn.
22. **Nhập bằng giọng nói**.
23. **Mẫu công việc / mẫu nhật ký**.
24. **Ma trận Eisenhower** (quan trọng – khẩn cấp).
25. **Tích hợp Google Calendar** (hai chiều) và Google Tasks.
26. **Thành tích / gamification** (huy hiệu, chuỗi ngày, cấp độ).
27. **Chế độ gia đình / nhóm** với bảng tin.
28. **Theo dõi thời gian thực tế** so với kế hoạch.

---

## 4. Gợi ý mô hình dữ liệu Firebase

```text
Tasks/{taskId}
  title, description, date, time, priority, icon, isCompleted,
  userId, timestamp,
  listId?, recurrence?, reminders?, subtaskCount?

Subtasks/{taskId}/{subtaskId}
  title, isDone, order

Lists/{listId}
  name, color, icon, ownerId, order

Habits/{habitId}
  name, icon, color, frequency, userId
HabitLogs/{habitId}/{yyyy-MM-dd}: true

Diaries/{diaryId}
  title, content, date, timestamp, userId,
  mood?, photos?, isPinned?
```

> Lưu ý: cập nhật Firebase Rules tương ứng khi thêm node mới.

---

## 5. Thứ tự triển khai đề xuất

1. P0: lặp lại + nhắc việc + tìm kiếm + danh sách/dự án + dark mode.
2. P1: chế độ tuần, quick add, thống kê, mood nhật ký.
3. P2: AI, tích hợp lịch, cộng tác.

Mỗi bước nên làm theo hướng: cập nhật model → repository/query → UI → kiểm thử trên thiết bị thật.
