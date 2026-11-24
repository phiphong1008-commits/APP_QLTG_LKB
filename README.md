# APP_QLTG — Quản lý thời gian & lập kế hoạch

Ứng dụng Android giúp bạn quản lý công việc hằng ngày, nhìn lịch trình theo tháng/tuần, ghi nhật ký,
xây dựng thói quen và theo dõi thành tích cá nhân. Dữ liệu được đồng bộ với **Firebase Realtime Database**
và vẫn dùng được khi thiết bị ngoại tuyến.

- **Ngôn ngữ:** Java (Android SDK 36, minSdk 24)
- **Backend:** Firebase Authentication + Firebase Realtime Database
- **Giao diện:** Material 3, hỗ trợ sáng/tối, 3 cỡ chữ
- **Video demo:** [Xem trên Google Drive](https://drive.google.com/file/d/1HA48yqNCzc4phmSN6zYNxDm4I4SZ44tX/view?usp=drive_link)

---

## 1. Đăng ký & đăng nhập

Tài khoản được xác thực bằng Firebase Authentication (email + mật khẩu), có kiểm tra định dạng email,
độ dài mật khẩu và xác nhận mật khẩu ngay trên màn hình.

<p align="center">
  <img src="docs/images/screens/01_dang_ky.png" width="260" alt="Màn hình đăng ký">
  <img src="docs/images/screens/02_dang_ky_da_dien.png" width="260" alt="Đăng ký khi đã nhập thông tin">
</p>

## 2. Danh sách công việc trong ngày

- Thẻ **Tiến độ hôm nay** hiển thị số việc đã xong / tổng số việc và lời nhắc còn lại bao nhiêu việc.
- Lọc nhanh theo trạng thái: **Tất cả / Chưa xong / Đã xong**.
- Lọc theo danh sách: **Mọi danh sách / Cá nhân / Học tập / Công việc**.
- Mỗi thẻ việc có loại việc (icon), giờ, mức ưu tiên, mô tả, danh sách và tiến độ việc con.

<p align="center">
  <img src="docs/images/screens/03_cong_viec.png" width="260" alt="Danh sách công việc hôm nay">
  <img src="docs/images/screens/04_cong_viec_chua_xong.png" width="260" alt="Lọc việc chưa xong">
  <img src="docs/images/screens/05_cong_viec_da_xong.png" width="260" alt="Lọc việc đã xong">
</p>

## 3. Tạo và chỉnh sửa công việc

Form công việc cho phép nhập tên, chọn loại (Học tập, Công việc, Nhà cửa, Sự kiện), mô tả chi tiết,
danh sách, việc con, ngày giờ, mức ưu tiên, lặp lại, nhắc nhở và vị trí.

<p align="center">
  <img src="docs/images/screens/06_tao_cong_viec.png" width="260" alt="Tạo công việc mới">
  <img src="docs/images/screens/07_sua_cong_viec.png" width="260" alt="Sửa công việc và việc con">
</p>

## 4. Thêm nhanh bằng câu tự nhiên

Gõ một câu như `Họp nhóm 9h mai !cao` — ứng dụng tự nhận diện **ngày, giờ, mức ưu tiên và danh sách**,
hiển thị bản xem trước ngay bên dưới trước khi thêm. Hỗ trợ cả nhập bằng giọng nói.

<p align="center">
  <img src="docs/images/screens/08_them_nhanh.png" width="260" alt="Hộp thoại thêm nhanh">
  <img src="docs/images/screens/09_them_nhanh_da_go.png" width="260" alt="Xem trước kết quả nhận diện">
</p>

## 5. Tìm kiếm & lọc công việc

Tìm theo tên, mô tả hoặc tên danh sách; kết quả cập nhật ngay khi gõ.

<p align="center">
  <img src="docs/images/screens/10_tim_kiem.png" width="260" alt="Tìm kiếm công việc">
</p>

## 6. Lịch tháng & lịch tuần

Xem công việc theo từng ngày trên lịch tháng, hoặc chuyển sang **Lịch tuần** để thấy toàn bộ tuần
với chấm đánh dấu những ngày có việc.

<p align="center">
  <img src="docs/images/screens/11_lich_thang.png" width="260" alt="Lịch tháng">
  <img src="docs/images/screens/12_lich_tuan.png" width="260" alt="Lịch tuần">
</p>

## 7. Nhật ký

- Danh sách nhật ký có ghim bài, tìm kiếm và lọc theo tháng.
- Trình soạn nhật ký với tâm trạng, thời tiết, gợi ý câu hỏi, ảnh đính kèm.
- **Khóa nhật ký bằng PIN 4–6 số**: mã chỉ lưu trên thiết bị, không gửi lên máy chủ.

<p align="center">
  <img src="docs/images/screens/13_nhat_ky.png" width="260" alt="Danh sách nhật ký">
  <img src="docs/images/screens/14_nhat_ky_dang_khoa.png" width="260" alt="Nhật ký đang khóa bằng PIN">
</p>
<p align="center">
  <img src="docs/images/screens/15_viet_nhat_ky.png" width="260" alt="Viết nhật ký mới">
  <img src="docs/images/screens/16_sua_nhat_ky.png" width="260" alt="Sửa nhật ký đã lưu">
</p>

## 8. Tài khoản & danh sách công việc

Hồ sơ hiển thị thống kê nhanh (tổng số việc, đã xong, chưa xong), cho phép đổi tên và màu đại diện.
Màn **Danh sách** quản lý các nhóm công việc, chia sẻ hoặc nhập danh sách được chia sẻ bằng mã.

<p align="center">
  <img src="docs/images/screens/17_ho_so.png" width="260" alt="Hồ sơ và thống kê">
  <img src="docs/images/screens/18_sua_ho_so.png" width="260" alt="Chỉnh sửa hồ sơ">
  <img src="docs/images/screens/19_danh_sach.png" width="260" alt="Quản lý danh sách">
</p>

## 9. Cài đặt, sao lưu & khóa nhật ký

- **Chế độ hiển thị:** theo hệ thống / sáng / tối.
- **Cỡ chữ:** nhỏ / vừa / lớn.
- **Khóa nhật ký:** bật, đổi hoặc tắt PIN.
- **Sao lưu & xuất dữ liệu:** JSON (bản sao lưu đầy đủ), CSV (công việc) và Markdown (bản đọc).

<p align="center">
  <img src="docs/images/screens/20_cai_dat.png" width="260" alt="Cài đặt giao diện và khóa nhật ký">
  <img src="docs/images/screens/21_cai_dat_sao_luu.png" width="260" alt="Sao lưu và xuất dữ liệu">
  <img src="docs/images/screens/22_dat_pin.png" width="260" alt="Đặt mã PIN cho nhật ký">
</p>

## 10. Bộ công cụ nâng cao

Tất cả công cụ nằm trong **Tài khoản → Khám phá tính năng**:

| Công cụ | Mô tả |
| --- | --- |
| **Lịch tuần** | Xem công việc theo tuần, chuyển tuần trước/sau. |
| **Thêm nhanh** | Phân tích câu tự nhiên để tạo việc trong vài giây. |
| **Tập trung** | Đồng hồ Pomodoro 15/25/45 phút, lưu phiên tập trung. |
| **Thói quen** | Theo dõi chuỗi ngày liên tiếp của từng thói quen. |
| **Thống kê** | Tỉ lệ hoàn thành, biểu đồ 7 ngày, phân bố theo mức ưu tiên. |
| **Ma trận Eisenhower** | Chia việc theo 4 ô: làm ngay, lên kế hoạch, chia sẻ, loại bỏ. |
| **Mẫu công việc** | Lưu việc quen thuộc thành mẫu và tạo lại trong một chạm. |
| **Thành tích** | Huy hiệu mở khóa theo tiến độ sử dụng. |
| **Trợ lý AI** | Phân tích dữ liệu ngay trên máy, gợi ý việc nên ưu tiên. |
| **Nhóm & gia đình** | Tạo/tham gia nhóm bằng mã 6 ký tự để chia sẻ việc chung. |

<p align="center">
  <img src="docs/images/screens/23_kham_pha_tinh_nang.png" width="260" alt="Khám phá tính năng">
  <img src="docs/images/screens/24_tap_trung.png" width="260" alt="Phiên tập trung Pomodoro">
  <img src="docs/images/screens/25_thoi_quen.png" width="260" alt="Theo dõi thói quen">
</p>
<p align="center">
  <img src="docs/images/screens/26_thong_ke.png" width="260" alt="Thống kê hoàn thành">
  <img src="docs/images/screens/27_ma_tran_eisenhower.png" width="260" alt="Ma trận Eisenhower">
  <img src="docs/images/screens/28_mau_cong_viec.png" width="260" alt="Mẫu công việc">
</p>
<p align="center">
  <img src="docs/images/screens/29_thanh_tich.png" width="260" alt="Thành tích">
  <img src="docs/images/screens/30_tro_ly_ai.png" width="260" alt="Trợ lý AI cục bộ">
  <img src="docs/images/screens/31_nhom_gia_dinh.png" width="260" alt="Nhóm và gia đình">
</p>

---

## Cấu trúc dữ liệu Firebase

```
Users/{uid}            → tên hiển thị, màu đại diện, email, danh sách nhóm
Tasks/{taskId}         → tiêu đề, mô tả, ngày, giờ, ưu tiên, lặp lại, việc con, listId, userId
Lists/{listId}         → tên danh sách, màu, mã chia sẻ, userId
Diaries/{diaryId}      → tiêu đề, nội dung, tâm trạng, thời tiết, ảnh (base64), ghim, userId
Habits/{habitId}       → tên, emoji, màu, userId
HabitLogs/{habitId}/{yyyy-MM-dd} → true khi đã hoàn thành
FocusSessions/{id}     → taskId, thời lượng, thời điểm bắt đầu/kết thúc, userId
Templates/{id}         → mẫu công việc để tạo lại nhanh
Groups/{code}          → tên nhóm, ownerId, members
```

Mọi truy vấn đều lọc theo `userId` (hoặc `ownerId` với `Groups`) và bật
`setPersistenceEnabled(true)` để đọc/ghi được khi mất mạng.

## Cài đặt & chạy

1. Mở project bằng **Android Studio** (JDK 11 trở lên).
2. Thêm `google-services.json` của Firebase vào thư mục `app/`
   (project phải bật **Authentication → Email/Password** và **Realtime Database**).
3. Build và chạy:

```bash
# Windows
gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug
```

APK đầu ra: `app/build/outputs/apk/debug/app-debug.apk`.

## Cấu trúc thư mục

```
app/src/main/java/dpphong/ntu/appqlcv/ck/
├── MainActivity.java            # Điều hướng chính, bottom navigation
├── LoginFragment.java           # Đăng nhập
├── RegisterFragment.java        # Đăng ký tài khoản
├── TodoFragment.java            # Danh sách công việc trong ngày
├── AddTodoFragment.java         # Tạo / sửa công việc
├── QuickAddDialogFragment.java  # Thêm nhanh bằng câu tự nhiên (+ QuickAddParser)
├── SearchFragment.java          # Tìm kiếm & lọc công việc
├── CalendarFragment.java        # Lịch tháng
├── WeekFragment.java            # Lịch tuần
├── DiaryFragment.java           # Danh sách nhật ký + khóa PIN
├── AddDiaryFragment.java        # Soạn / sửa nhật ký
├── ListAdapter.java             # Danh sách công việc (model TaskList)
├── ListsFragment.java           # Quản lý danh sách & chia sẻ
├── ProfileFragment.java         # Hồ sơ, thống kê, lối tắt
├── EditProfileFragment.java     # Chỉnh sửa hồ sơ
├── SettingsFragment.java        # Giao diện, khóa PIN, xuất dữ liệu
├── ToolsFragment.java           # Khám phá tính năng
├── FocusFragment.java           # Pomodoro
├── HabitsFragment.java          # Thói quen
├── StatsFragment.java           # Thống kê
├── MatrixFragment.java          # Ma trận Eisenhower
├── TemplatesFragment.java       # Mẫu công việc
├── AchievementsFragment.java    # Thành tích
├── AssistantFragment.java       # Trợ lý AI cục bộ
├── GroupsFragment.java          # Nhóm & gia đình
├── Task.java, Subtask.java, TaskList.java, TaskTemplate.java
├── Diary.java, Habit.java, FocusSession.java
├── Prefs.java                   # Cài đặt cục bộ (giao diện, cỡ chữ, PIN)
├── ReminderScheduler.java       # Nhắc nhở & NotificationReceiver
├── TaskCache.java               # Bộ nhớ đệm công việc
└── BarChartView.java            # Biểu đồ cột cho màn Thống kê
```

## Ghi chú thiết kế

- Nhánh giao diện mới: `feature/ui-redesign`
- Tóm tắt thay đổi giao diện và danh sách tính năng gợi ý:
  [docs/UI_REDESIGN_VA_GOI_Y_TINH_NANG.md](docs/UI_REDESIGN_VA_GOI_Y_TINH_NANG.md)

## Ảnh chụp màn hình

Toàn bộ ảnh nằm trong [`docs/images/screens`](docs/images/screens) và được chụp từ bản build thật
của ứng dụng trên Android 16 (emulator Pixel).
