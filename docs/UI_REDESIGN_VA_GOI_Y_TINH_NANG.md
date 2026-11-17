# Thiết kế lại giao diện & gợi ý tính năng

> Nhánh: `feature/ui-redesign` (P0) → `feature/p1-p2` (P1 + P2)  
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

## 3. Danh sách tính năng &amp; trạng thái triển khai

### P0 — Đã triển khai (nhánh `feature/ui-redesign`)
1. ✅ **Công việc lặp lại** hằng ngày / tuần / tháng (`recurrence`, `nextSpawned`).
   - Khi đánh dấu hoàn thành, `TaskAdapter` tự tạo bản ghi kỳ kế tiếp và đặt lại nhắc việc.
2. ✅ **Nhiều lời nhắc cho một việc** (`reminderOffsets`: đúng giờ, trước 10 phút, 1 giờ, 1 ngày).
   - `ReminderScheduler` đặt/hủy báo thức, `NotificationReceiver` hiển thị đúng mốc.
3. ✅ **Tìm kiếm & lọc nâng cao** (`SearchFragment`): từ khóa, trạng thái, ưu tiên, khoảng ngày.
4. ✅ **Danh sách / dự án** (`ListsFragment`, node `Lists`, `listId/listName/listColor` trong `Task`).
   - Chip lọc danh sách ngay trên màn hình Công việc.
5. ✅ **Việc con (subtasks)** và tiến độ `2/5`; chạm vào tiến độ để đánh dấu nhanh.
   - Lưu dạng mảng `subtasks` trong `Task` để đơn giản hóa.
6. ✅ **Chỉnh sửa hồ sơ** (`EditProfileFragment`): tên hiển thị, màu đại diện, đổi mật khẩu qua email.
   - Lưu thêm node `Users/{uid}/displayName, avatarColor`.
7. ✅ **Chế độ tối và cỡ chữ** (`SettingsFragment`, `Prefs`, `values-night`).
   - Chọn Theo hệ thống / Sáng / Tối; cỡ chữ Nhỏ / Vừa / Lớn.
8. ✅ **Lưu offline & đồng bộ** — bật Firebase persistence trong `App`.
9. ✅ **Widget màn hình chính** (`TodayWidgetProvider`): tiến độ và 5 việc hôm nay.
10. ✅ **Xuất / sao lưu dữ liệu** (`SettingsFragment`): JSON (công việc + nhật ký), CSV (công việc), Markdown.

> Đã build thành công bằng `.\gradlew.bat assembleDebug --offline`. Chưa kiểm thử trực quan trên thiết bị thật do môi trường không có emulator.

### P1 — Nâng cấp trải nghiệm (✅ đã triển khai trên `feature/p1-p2`)
11. ✅ **Chế độ xem Tuần / Agenda** — `WeekFragment` + `fragment_week.xml`.
    - 7 ô ngày trong tuần, chấm đánh dấu ngày có việc, chuyển tuần trước/sau, chạm một ngày để xem danh sách công việc, FAB thêm việc đúng ngày đang chọn.
    - Đổi ngày: **nhấn giữ công việc → “Dời sang ngày khác”** rồi chọn ngày bằng date picker (chưa phải kéo–thả trực tiếp).
12. ✅ **Quick Add bằng ngôn ngữ tự nhiên** — `QuickAddParser`, `QuickAddDialogFragment`.
    - Hiểu giờ (`9h`, `9:30`, `21g`), ngày (`hôm nay`, `mai`, `ngày kia`, `thứ 2…CN`, `dd/MM`), ưu tiên (`!cao`, `!vừa`, `!thấp`, `P1…P3`) và danh sách (`#tên`).
    - Hiển thị bản xem trước trước khi lưu; có nút micro nhập bằng giọng nói.
13. ✅ **Pomodoro / chế độ tập trung** — `FocusFragment`, node `FocusSessions`.
    - Chọn 15/25/45/60 phút, đếm ngược, bắt đầu/tạm dừng/đặt lại, lưu phiên khi hoàn tất, danh sách phiên gần đây và tổng phút hôm nay.
    - Mở từ công cụ, hoặc từ menu thao tác của công việc (“Tập trung”).
14. ✅ **Thói quen & streak** — `HabitsFragment`, `HabitAdapter`, node `Habits` + `HabitLogs`.
    - Thêm thói quen (tên, icon, số ngày mục tiêu/tuần), đánh dấu hoàn thành theo từng ngày, hiển thị chuỗi ngày liên tiếp và 7 chấm tuần.
15. ✅ **Thống kê & biểu đồ** — `StatsFragment` + `BarChartView` tự vẽ (không thêm thư viện).
    - Tỉ lệ hoàn thành, biểu đồ 7 ngày, thanh theo mức ưu tiên, phút tập trung hôm nay, thói quen hôm nay, số bài nhật ký, tiến độ theo từng danh sách.
16. ✅ **Nhật ký nâng cao** — `AddDiaryFragment`, `fragment_diary.xml`, model `Diary`.
    - Chip cảm xúc (vui/bình thường/buồn/tức giận/mệt), thời tiết (nắng/mây/mưa/lạnh), prompt gợi ý, chọn ảnh (thu nhỏ và lưu base64 trong `photoBase64`), xem/xóa ảnh.
17. ✅ **Khóa nhật ký bằng PIN** — `Prefs`, `fragment_settings.xml`, `DiaryFragment`.
    - Đặt/đổi/tắt PIN 4–6 số trong Cài đặt; màn hình nhật ký hiện lớp khóa, nhập PIN mới xem được; có nút khóa nhanh.
    - Chưa dùng vân tay/BiometricPrompt (PIN lưu cục bộ trên thiết bị).
18. ✅ **Tìm kiếm nhật ký, ghim bài, lịch sử theo tháng** — `DiaryFragment`, `DiaryAdapter`.
    - Tìm theo tiêu đề/nội dung, lọc Tất cả / Tháng này / Tháng trước, ghim lên đầu, menu nhấn giữ để ghim hoặc xóa.
19. ✅ **Nhắc theo vị trí** — `LocationHelper`, thẻ “Nhắc theo vị trí” trong `fragment_add_todo.xml`.
    - Lưu tên + tọa độ vào công việc; khi mở app, kiểm tra khoảng cách (bán kính 250 m) và gửi thông báo, mỗi việc tối đa một lần/ngày.
    - Chỉ kiểm tra khi app được mở (`MainActivity.onResume`), không chạy nền/geofencing.
20. ✅ **Chia sẻ danh sách** — `ListsFragment`, `ListAdapter`, node `SharedLists`.
    - Tạo mã chia sẻ 6 ký tự, chụp nội dung danh sách + công việc lên `SharedLists/{code}`; người nhận nhập mã để **sao chép** danh sách về tài khoản.
    - Là bản sao tại thời điểm chia sẻ, chưa phải cộng tác trực tiếp/thời gian thực.

### P2 — Khác biệt hóa (✅ đã triển khai trên `feature/p1-p2`)
21. ✅ **Trợ lý AI (cục bộ)** — `AssistantFragment`.
    - Tổng hợp công việc hôm nay, việc quá hạn, phút tập trung, thói quen và nhật ký để đưa ra gợi ý; chọn giọng điệu (thân thiện / ngắn gọn / động viên) lưu trong `Prefs`; có lối tắt thêm việc nhanh.
    - Dùng luật cục bộ, không gọi dịch vụ AI/LLM bên ngoài.
22. ✅ **Nhập bằng giọng nói** — `RecognizerIntent` trong `QuickAddDialogFragment` (nút micro).
23. ✅ **Mẫu công việc** — `TemplatesFragment`, `TemplateAdapter`, node `Templates`.
    - Lưu công việc hiện tại thành mẫu từ menu thao tác, áp dụng mẫu để tạo việc mới, xóa mẫu.
24. ✅ **Ma trận Eisenhower** — `MatrixFragment`, `fragment_matrix.xml`.
    - 4 góc phần tư; nhấn giữ công việc để đổi mức ưu tiên tương ứng.
25. ✅ **Tích hợp Google Calendar (một chiều)** — `CalendarHelper`.
    - Từ menu công việc chọn “Thêm vào Google Calendar”, mở màn hình chèn sự kiện của `CalendarContract` (cần app Google Calendar trên máy).
    - Chưa đồng bộ hai chiều/đọc sự kiện từ Google.
26. ✅ **Thành tích / gamification** — `AchievementsFragment`.
    - 10 huy hiệu dựa trên số việc hoàn thành, phút tập trung, chuỗi thói quen, số bài nhật ký.
27. ✅ **Nhóm / gia đình** — `GroupsFragment`, node `Groups`, `Users/{uid}/groups`.
    - Tạo nhóm (mã 6 ký tự) hoặc tham gia bằng mã, xem danh sách thành viên, chia sẻ mã mời, rời nhóm.
    - Hiện là danh sách thành viên + mã mời; chưa có bảng tin hay bảng công việc chung.
28. ✅ **Theo dõi thời gian (một phần)** — mỗi phiên tập trung lưu `minutes`, `taskId`, `taskTitle`, `startedAt`.
    - Thống kê tổng phút tập trung theo ngày; chưa có báo cáo so sánh thời gian thực tế với kế hoạch.

> Đã build thành công bằng `.\gradlew.bat assembleDebug --offline`. Chưa kiểm thử trực quan trên thiết bị thật do môi trường không có emulator.

### Điểm vào các tính năng mới
| Khu vực | Điểm vào |
| --- | --- |
| Tài khoản | Nút **Công cụ** → `ToolsFragment` (Lịch tuần, Thêm nhanh, Tập trung, Thói quen, Thống kê, Ma trận, Mẫu, Thành tích, Trợ lý AI, Nhóm) |
| Lịch | Nút **Lịch tuần** trên màn hình Lịch |
| Công việc | Nút **Thêm nhanh** trên màn hình Công việc; nhấn giữ một công việc để mở menu thao tác |
| Cài đặt | Thẻ **Khóa nhật ký** |
| Danh sách | Nút **chia sẻ** trên từng danh sách, nút **tải xuống** để nhập mã chia sẻ |
| Thêm/sửa công việc | Thẻ **Nhắc theo vị trí** |

---

## 4. Mô hình dữ liệu Firebase (đã dùng)

```text
Tasks/{taskId}
  title, description, date, time, priority, icon, isCompleted,
  userId, timestamp,
  listId?, listName?, listColor?,
  recurrence ("none" | "daily" | "weekly" | "monthly"),
  reminderOffsets: [0, 10, 60, 1440],   // phút trước giờ thực hiện
  subtasks: [{ id, title, completed }],
  nextSpawned: boolean,                  // đã tạo kỳ lặp kế tiếp chưa
  locationName?, locationLat?, locationLng?,
  completedAt                              // thời điểm hoàn thành (millis)

Lists/{listId}
  name, color, userId, timestamp, shareCode?

Users/{uid}
  displayName, avatarColor, email
  groups: { groupCode: true }             // các nhóm đã tham gia

Diaries/{diaryId}
  title, content, date, timestamp, userId,
  mood ("happy" | "ok" | "sad" | "angry" | "tired"),
  weather ("sun" | "cloud" | "rain" | "cold"),
  prompt?, photoBase64?, pinned: boolean

Habits/{habitId}
  name, icon, targetPerWeek, userId, timestamp

HabitLogs/{habitId}/{yyyy-MM-dd}: true

FocusSessions/{sessionId}
  userId, taskId?, taskTitle?, minutes, startedAt, createdAt

Templates/{templateId}
  title, description, priority, icon, recurrence, userId, timestamp

Groups/{groupCode}
  code, name, ownerId, createdAt
  members: { uid: { name, email, joinedAt } }

SharedLists/{shareCode}
  code, name, color, ownerId, createdAt, taskCount,
  tasks: { taskId: { title, description, date, time, priority, icon, recurrence } }

// Dữ liệu cục bộ (không lên Firebase)
SharedPreferences "app_settings": night_mode, font_scale, diary_pin, ai_tone
SharedPreferences "location_reminders": { taskId: "yyyy-MM-dd" }  // chống nhắc lặp trong ngày
```

> Lưu ý: cập nhật Firebase Rules tương ứng khi thêm node mới (`Habits`, `HabitLogs`, `FocusSessions`, `Templates`, `Groups`, `SharedLists`). Riêng `SharedLists` cần cho phép đọc theo mã chia sẻ.

---

## 5. Trạng thái triển khai & hạn chế đã biết

### Đã xong
1. P0 trên `feature/ui-redesign` (commit `72c6b27`): giao diện mới + 10 tính năng P0.
2. P1 (11–20) và P2 (21–28) trên `feature/p1-p2`: toàn bộ mục nêu trên đã có màn hình và luồng dữ liệu Firebase.
3. Kiểm tra build: `.\gradlew.bat assembleDebug --offline` thành công, APK tại
   `app\build\outputs\apk\debug\app-debug.apk`.

### Hạn chế
- **Chưa kiểm thử runtime**: môi trường không có emulator/thiết bị, mới xác nhận biên dịch.
- **Khóa nhật ký**: chỉ PIN cục bộ, chưa có vân tay/sinh trắc học; PIN lưu dạng văn bản trong SharedPreferences của app.
- **Nhắc theo vị trí**: chỉ kiểm tra khi mở app, không có geofencing chạy nền; bán kính cố định 250 m.
- **Chia sẻ danh sách**: là bản sao (snapshot) tại thời điểm chia sẻ, không đồng bộ trực tiếp; chưa phân quyền thành viên.
- **Google Calendar**: chỉ thêm sự kiện một chiều qua Intent, không đọc/đồng bộ ngược.
- **AI assistant**: dựa trên luật cục bộ, không phải mô hình ngôn ngữ.
- **Kéo–thả đổi ngày**: thay bằng thao tác nhấn giữ → chọn ngày.
- **Nhóm/gia đình**: mới có mã mời + danh sách thành viên, chưa có bảng tin/bảng việc chung.
- **Theo dõi thời gian**: mới có phút tập trung, chưa so sánh kế hoạch với thực tế.

---

## 6. Hướng phát triển tiếp
1. Kéo–thả thật trên chế độ Tuần và đồng bộ hai chiều với Google Calendar.
2. Geofencing chạy nền (WorkManager + Play Services) cho nhắc theo vị trí.
3. Cộng tác thời gian thực cho danh sách/nhóm (Firebase Rules + listener theo thành viên).
4. Sinh trắc học cho khóa nhật ký và mã hóa PIN.
5. Báo cáo so sánh thời gian kế hoạch – thực tế, xuất báo cáo tuần/tháng.

