package dpphong.ntu.appqlcv.ck;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView rvTasks;
    private TaskAdapter taskAdapter;
    private List<Task> allTasks; // Giả lập database chứa toàn bộ task

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        TextView tvHeaderTitle = view.findViewById(R.id.tv_header_title);
        if(tvHeaderTitle != null) {
            tvHeaderTitle.setText("Lịch & Công Việc");
        }

        calendarView = view.findViewById(R.id.calendarView);
        rvTasks = view.findViewById(R.id.rv_tasks);

        // Khởi tạo danh sách dummy data (Sau này lấy từ Firebase)
        initDummyData();

        // Cài đặt RecyclerView
        rvTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        taskAdapter = new TaskAdapter(new ArrayList<>());
        rvTasks.setAdapter(taskAdapter);

        // Lấy ngày hiện tại để hiển thị task của ngày hôm nay lúc mới mở lên
        Calendar calendar = Calendar.getInstance();
        String today = String.format("%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
        filterTasksByDate(today);

        // Bắt sự kiện khi người dùng click vào một ngày trên lịch
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                // Lưu ý: month trả về từ 0-11 nên cần cộng thêm 1
                String selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                filterTasksByDate(selectedDate);
            }
        });

        return view;
    }

    // Hàm lọc công việc theo ngày được chọn
    private void filterTasksByDate(String date) {
        List<Task> filteredList = new ArrayList<>();
        for (Task task : allTasks) {
            if (task.getDate().equals(date)) {
                filteredList.add(task);
            }
        }
        // Cập nhật lại giao diện danh sách
        taskAdapter.updateList(filteredList);
    }

    // Giả lập dữ liệu nhận về từ Database
    private void initDummyData() {
        allTasks = new ArrayList<>();

        // Bạn cần thay R.drawable.ic_... bằng các icon bạn đã chuẩn bị nhé
        // Dữ liệu mẫu cho ngày 2026-05-18
        allTasks.add(new Task("Học Lập trình Di động", "Hoàn thiện Fragment Lịch và Adapter", "2026-05-18", "Cao", android.R.drawable.ic_menu_edit));
        allTasks.add(new Task("Mua đồ siêu thị", "Mua sữa, rau, thịt bò", "2026-05-18", "Thấp", android.R.drawable.ic_menu_compass));

        // Dữ liệu mẫu cho ngày 2026-05-19
        allTasks.add(new Task("Họp nhóm đồ án", "Báo cáo tiến độ đồ án cuối kỳ", "2026-05-19", "Vừa", android.R.drawable.ic_menu_agenda));
    }
}