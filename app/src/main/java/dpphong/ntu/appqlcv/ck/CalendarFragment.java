package dpphong.ntu.appqlcv.ck;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class CalendarFragment extends Fragment {

    private TextView tvMonthYear;
    private GridView gvCalendar;
    private ArrayList<String> daysInMonthList;
    private Calendar selectedDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        TextView tvHeaderTitle = view.findViewById(R.id.tv_header_title);

        // 2. Cài đặt lại chữ theo đúng tên Fragment
        tvHeaderTitle.setText("Lịch");
        tvMonthYear = view.findViewById(R.id.tv_month_year);
        gvCalendar = view.findViewById(R.id.gv_calendar);

        selectedDate = Calendar.getInstance(); // Lấy thời gian hiện tại

        // 1. Giả lập dữ liệu nhận về từ Firebase (Sau này kết nối Firebase Realtime bạn chỉ cần nạp vào đây)
        HashMap<String, String> dataFromFirebase = new HashMap<>();
        dataFromFirebase.put("2026-05-18", "STUDY");   // Ngày 18 có việc học
        dataFromFirebase.put("2026-05-19", "MEETING"); // Ngày 19 có họp nhóm

        // 2. Thiết lập tiêu đề Tháng/Năm
        int month = selectedDate.get(Calendar.MONTH) + 1;
        int year = selectedDate.get(Calendar.YEAR);
        tvMonthYear.setText("Tháng " + String.format("%02d", month) + " / " + year);

        // 3. Tạo chuỗi định dạng "yyyy-MM" phục vụ so khớp key dữ liệu
        String currentMonthYearStr = String.format("%04d-%02d", year, month);

        // 4. Chạy hàm tính toán số ngày trong tháng đưa vào mảng danh sách
        daysInMonthList = generateDaysInMonth(selectedDate);

        // 5. Khởi tạo và nạp Adapter vào GridView
        CalendarAdapter calendarAdapter = new CalendarAdapter(
                getContext(),
                daysInMonthList,
                currentMonthYearStr,
                dataFromFirebase
        );
        gvCalendar.setAdapter(calendarAdapter);

        return view;
    }

    // Thuật toán Java tự động tính toán vị trí ô trống đầu tháng và số ngày trong tháng
    private ArrayList<String> generateDaysInMonth(Calendar calendar) {
        ArrayList<String> daysList = new ArrayList<>();
        Calendar cal = (Calendar) calendar.clone();

        cal.set(Calendar.DAY_OF_MONTH, 1);
        int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Lấy thứ của ngày đầu tiên trong tháng (Chủ nhật = 1, Thứ hai = 2, ...)
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

        // Chuyển đổi thuật toán dịch ô trống cho khớp từ Thứ hai đến Chủ nhật
        int spaces = firstDayOfWeek == Calendar.SUNDAY ? 6 : firstDayOfWeek - 2;

        // Thêm các ô trống ở đầu lưới
        for (int i = 0; i < spaces; i++) {
            daysList.add("");
        }

        // Điền các ngày trong tháng từ 1 đến hết
        for (int i = 1; i <= maxDays; i++) {
            daysList.add(String.valueOf(i));
        }

        return daysList;
    }
}