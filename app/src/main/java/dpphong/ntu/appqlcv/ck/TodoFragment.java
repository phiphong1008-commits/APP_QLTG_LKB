package dpphong.ntu.appqlcv.ck;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TodoFragment extends Fragment {

    private RecyclerView rvTodoToday;
    private TextView tvHeaderTitle;
    private FloatingActionButton fabAddTodo;
    private TaskAdapter taskAdapter;
    private List<Task> todayTasks;
    private String todayString;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_todo, container, false);

        rvTodoToday = view.findViewById(R.id.rv_todo_today);
        fabAddTodo = view.findViewById(R.id.fab_add_todo);
        tvHeaderTitle = view.findViewById(R.id.tv_header_title);
        tvHeaderTitle.setText("Danh sách công việc");
        // Khởi tạo RecyclerView
        todayTasks = new ArrayList<>();
        taskAdapter = new TaskAdapter(todayTasks);
        rvTodoToday.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTodoToday.setAdapter(taskAdapter);

        // Lấy chuỗi ngày hôm nay (VD: "2026-05-31")
        Calendar calendar = Calendar.getInstance();
        todayString = String.format("%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));

        // Tải dữ liệu công việc hôm nay
        loadTodayTasks();

        // Xử lý sự kiện khi ấn nút (+)
        fabAddTodo.setOnClickListener(v -> {
            // Chuyển sang màn hình thêm công việc (AddTodoFragment)
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.calendar_container, new AddTodoFragment()) // Tên class nhập liệu của bạn
                    .addToBackStack(null) // Lệnh này giúp ấn nút Back trên điện thoại sẽ quay lại danh sách
                    .commit();
        });

        return view;
    }

    private void loadTodayTasks() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String currentUserId = currentUser.getUid();

        // Truy vấn lấy task của user hiện tại
        Query query = FirebaseDatabase.getInstance().getReference("Tasks")
                .orderByChild("userId").equalTo(currentUserId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                todayTasks.clear();
                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    Task task = taskSnapshot.getValue(Task.class);
                    // Chỉ lấy những task có ngày trùng với hôm nay
                    if (task != null && task.getDate() != null && task.getDate().equals(todayString)) {
                        todayTasks.add(task);
                    }
                }
                // Cập nhật lên màn hình
                taskAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi tải todo: " + error.getMessage());
            }
        });
    }
}
