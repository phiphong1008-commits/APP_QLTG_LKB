package dpphong.ntu.appqlcv.ck;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView rvTasks;
    private TaskAdapter taskAdapter;
    private List<Task> allTasks;

    // Thêm biến này để lưu ngày người dùng đang chọn trên lịch
    private String currentSelectedDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        TextView tvHeaderTitle = view.findViewById(R.id.tv_header_title);
        if(tvHeaderTitle != null) {
            tvHeaderTitle.setText("Lịch");
        }

        calendarView = view.findViewById(R.id.calendarView);
        rvTasks = view.findViewById(R.id.rv_tasks);

        // 1. Khởi tạo danh sách rỗng để tránh NullPointerException
        allTasks = new ArrayList<>();

        // 2. Cài đặt RecyclerView
        rvTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        taskAdapter = new TaskAdapter(new ArrayList<>());
        rvTasks.setAdapter(taskAdapter);

        // 3. Lấy ngày hiện tại hệ thống để gán cho currentSelectedDate lúc mới mở lên
        Calendar calendar = Calendar.getInstance();
        currentSelectedDate = String.format("%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));

        // 4. Bắt đầu tải dữ liệu thực tế từ Firebase
        loadTasksFromFirebase();

        // 5. Bắt sự kiện khi người dùng click vào một ngày bất kỳ trên lịch
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                // Cập nhật lại ngày đang chọn
                currentSelectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                // Tiến hành lọc dữ liệu
                filterTasksByDate(currentSelectedDate);
            }
        });

        return view;
    }

    // Hàm lọc công việc theo ngày được chọn
    private void filterTasksByDate(String date) {
        List<Task> filteredList = new ArrayList<>();
        for (Task task : allTasks) {
            // Cần check task.getDate() != null để tránh crash app nếu trên db có task bị lỗi thiếu ngày
            if (task.getDate() != null && task.getDate().equals(date)) {
                filteredList.add(task);
            }
        }
        // Cập nhật lại giao diện danh sách
        taskAdapter.updateList(filteredList);
    }

    // Hàm tải dữ liệu Realtime từ Firebase
    private void loadTasksFromFirebase() {
        // Lấy ID của user đang đăng nhập
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Truy vấn: Tìm trong node "Tasks", lấy ra các dữ liệu có trường "userId" khớp với user hiện tại
        Query query = FirebaseDatabase.getInstance().getReference("Tasks")
                .orderByChild("userId").equalTo(currentUserId);

        // Lắng nghe dữ liệu, khi có thêm/sửa/xóa trên Firebase thì nó tự cập nhật lại
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allTasks.clear(); // Xóa list cũ trước khi add list mới vào
                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    Task task = taskSnapshot.getValue(Task.class);
                    if (task != null) {
                        allTasks.add(task);
                    }
                }
                // Sau khi lấy toàn bộ data về, chạy hàm lọc theo ngày đang chọn trên lịch để hiển thị
                filterTasksByDate(currentSelectedDate);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi tải dữ liệu: " + error.getMessage());
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}