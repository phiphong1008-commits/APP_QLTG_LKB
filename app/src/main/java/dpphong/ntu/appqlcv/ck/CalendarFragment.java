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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView rvTasks;
    private TaskAdapter taskAdapter;
    private List<Task> allTasks;
    private View layoutEmptyTasks;
    private TextView tvTaskCount;
    private TextView tvSelectedDate;

    // Ngày người dùng đang chọn trên lịch (định dạng yyyy-MM-dd)
    private String currentSelectedDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        rvTasks = view.findViewById(R.id.rv_tasks);
        layoutEmptyTasks = view.findViewById(R.id.layout_empty_tasks);
        tvTaskCount = view.findViewById(R.id.tv_task_count);
        tvSelectedDate = view.findViewById(R.id.tv_selected_date);
        TextView tvTodaySubtitle = view.findViewById(R.id.tv_today_subtitle);
        TextView btnToday = view.findViewById(R.id.btn_today);
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add_calendar_task);

        // 1. Khởi tạo danh sách rỗng để tránh NullPointerException
        allTasks = new ArrayList<>();

        // 2. Cài đặt RecyclerView
        rvTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        taskAdapter = new TaskAdapter(new ArrayList<>());
        rvTasks.setAdapter(taskAdapter);

        // 3. Lấy ngày hiện tại hệ thống
        Calendar calendar = Calendar.getInstance();
        currentSelectedDate = String.format("%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));

        tvTodaySubtitle.setText(formatToday(calendar.getTime()));
        updateHeader(currentSelectedDate, 0);

        // 4. Tải dữ liệu thực tế từ Firebase
        loadTasksFromFirebase();

        // 5. Bắt sự kiện chọn ngày trên lịch
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            currentSelectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            filterTasksByDate(currentSelectedDate);
        });

        // 6. Quay nhanh về hôm nay
        btnToday.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            currentSelectedDate = String.format("%04d-%02d-%02d",
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH) + 1,
                    now.get(Calendar.DAY_OF_MONTH));
            calendarView.setDate(now.getTimeInMillis());
            filterTasksByDate(currentSelectedDate);
        });

        // 7. Thêm việc ngay cho ngày đang chọn
        fabAdd.setOnClickListener(v -> {
            AddTodoFragment addFragment = new AddTodoFragment();
            Bundle args = new Bundle();
            args.putString("TASK_DATE", currentSelectedDate);
            addFragment.setArguments(args);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.calendar_container, addFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private String formatToday(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd 'tháng' MM", new Locale("vi", "VN"));
        return capitalize(sdf.format(date));
    }

    private String formatSelectedDate(String dateStr) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = input.parse(dateStr);
            if (date == null) return dateStr;
            SimpleDateFormat output = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
            return capitalize(output.format(date));
        } catch (Exception e) {
            return dateStr;
        }
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private void updateHeader(String date, int taskCount) {
        if (tvSelectedDate != null) {
            tvSelectedDate.setText(formatSelectedDate(date));
        }
        if (tvTaskCount != null) {
            tvTaskCount.setText(taskCount + " việc");
        }
    }

    // Hàm lọc công việc theo ngày được chọn
    private void filterTasksByDate(String date) {
        List<Task> filteredList = new ArrayList<>();
        for (Task task : allTasks) {
            if (task.getDate() != null && task.getDate().equals(date)) {
                filteredList.add(task);
            }
        }
        if (taskAdapter != null) {
            taskAdapter.updateList(filteredList);
        }
        updateHeader(date, filteredList.size());

        boolean isEmpty = filteredList.isEmpty();
        if (layoutEmptyTasks != null) {
            layoutEmptyTasks.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
        if (rvTasks != null) {
            rvTasks.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    // Hàm tải dữ liệu Realtime từ Firebase
    private void loadTasksFromFirebase() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            filterTasksByDate(currentSelectedDate);
            return;
        }
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Query query = FirebaseDatabase.getInstance().getReference("Tasks")
                .orderByChild("userId").equalTo(currentUserId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allTasks.clear();
                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    Task task = taskSnapshot.getValue(Task.class);
                    if (task != null) {
                        allTasks.add(task);
                    }
                }
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
