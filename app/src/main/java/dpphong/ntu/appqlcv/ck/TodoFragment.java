package dpphong.ntu.appqlcv.ck;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class TodoFragment extends Fragment {

    private RecyclerView rvTodoToday;
    private FloatingActionButton fabAddTodo;
    private TaskAdapter taskAdapter;
    private List<Task> todayTasks;
    private String todayString;

    private LinearProgressIndicator progressToday;
    private TextView tvProgressValue, tvProgressMessage;
    private TextView tvEmptyTitle, tvEmptyMessage;
    private View layoutEmptyTodo;

    private Chip chipAll;
    private int filterMode = 0; // 0: tất cả, 1: chưa xong, 2: đã xong

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_todo, container, false);

        rvTodoToday = view.findViewById(R.id.rv_todo_today);
        fabAddTodo = view.findViewById(R.id.fab_add_todo);
        progressToday = view.findViewById(R.id.progress_today);
        tvProgressValue = view.findViewById(R.id.tv_progress_value);
        tvProgressMessage = view.findViewById(R.id.tv_progress_message);
        tvEmptyTitle = view.findViewById(R.id.tv_empty_title);
        tvEmptyMessage = view.findViewById(R.id.tv_empty_message);
        layoutEmptyTodo = view.findViewById(R.id.layout_empty_todo);
        chipAll = view.findViewById(R.id.chip_all);
        Chip chipPending = view.findViewById(R.id.chip_pending);
        Chip chipDone = view.findViewById(R.id.chip_done);
        TextView tvSubtitle = view.findViewById(R.id.tv_todo_subtitle);

        // Ngày hôm nay hiển thị thân thiện
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM", new Locale("vi", "VN"));
        String dateText = sdf.format(new Date());
        tvSubtitle.setText(Character.toUpperCase(dateText.charAt(0)) + dateText.substring(1) + " • Hôm nay");

        // Khởi tạo RecyclerView
        todayTasks = new ArrayList<>();
        taskAdapter = new TaskAdapter(new ArrayList<>());
        rvTodoToday.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTodoToday.setAdapter(taskAdapter);

        // Lấy chuỗi ngày hôm nay (VD: "2026-05-31")
        Calendar calendar = Calendar.getInstance();
        todayString = String.format("%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));

        // Bộ lọc trạng thái
        chipAll.setOnClickListener(v -> {
            filterMode = 0;
            renderTasks();
        });
        chipPending.setOnClickListener(v -> {
            filterMode = 1;
            renderTasks();
        });
        chipDone.setOnClickListener(v -> {
            filterMode = 2;
            renderTasks();
        });

        // Tải dữ liệu công việc hôm nay
        loadTodayTasks();

        // Xử lý sự kiện khi ấn nút (+)
        fabAddTodo.setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .replace(R.id.calendar_container, new AddTodoFragment())
                .addToBackStack(null)
                .commit());

        return view;
    }

    private void loadTodayTasks() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            renderTasks();
            return;
        }

        String currentUserId = currentUser.getUid();

        Query query = FirebaseDatabase.getInstance().getReference("Tasks")
                .orderByChild("userId").equalTo(currentUserId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                todayTasks.clear();
                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    Task task = taskSnapshot.getValue(Task.class);
                    if (task != null && task.getDate() != null && task.getDate().equals(todayString)) {
                        todayTasks.add(task);
                    }
                }
                renderTasks();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi tải todo: " + error.getMessage());
            }
        });
    }

    /**
     * Tính tiến độ, lọc danh sách theo chip đang chọn và cập nhật giao diện.
     */
    private void renderTasks() {
        if (!isAdded()) return;

        int total = todayTasks.size();
        int completed = 0;
        for (Task task : todayTasks) {
            if (task.isCompleted()) completed++;
        }

        int percent = total == 0 ? 0 : Math.round(completed * 100f / total);
        progressToday.setProgressCompat(percent, true);
        tvProgressValue.setText(completed + "/" + total + " việc");

        if (total == 0) {
            tvProgressMessage.setText("Chưa có việc nào hôm nay");
        } else if (completed == total) {
            tvProgressMessage.setText("Tuyệt vời! Bạn đã hoàn thành tất cả 🎉");
        } else {
            tvProgressMessage.setText("Còn " + (total - completed) + " việc chưa hoàn thành");
        }

        List<Task> display = new ArrayList<>();
        for (Task task : todayTasks) {
            if (filterMode == 1 && task.isCompleted()) continue;
            if (filterMode == 2 && !task.isCompleted()) continue;
            display.add(task);
        }
        taskAdapter.updateList(display);

        boolean isEmpty = display.isEmpty();
        layoutEmptyTodo.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvTodoToday.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (isEmpty) {
            if (total == 0) {
                tvEmptyTitle.setText("Hôm nay chưa có việc nào");
                tvEmptyMessage.setText("Nhấn nút + để thêm việc mới nhé");
            } else if (filterMode == 1) {
                tvEmptyTitle.setText("Không còn việc nào đang chờ");
                tvEmptyMessage.setText("Bạn đã hoàn thành hết việc hôm nay 🎉");
            } else if (filterMode == 2) {
                tvEmptyTitle.setText("Chưa có việc nào hoàn thành");
                tvEmptyMessage.setText("Đánh dấu xong một việc để thấy tiến độ nhé");
            } else {
                tvEmptyTitle.setText("Chưa có việc nào");
                tvEmptyMessage.setText("Nhấn nút + để thêm việc mới nhé");
            }
        }

        // Đảm bảo chip Tất cả luôn được chọn khi mở màn hình lần đầu
        if (filterMode == 0 && !chipAll.isChecked()) {
            chipAll.setChecked(true);
        }
    }
}
