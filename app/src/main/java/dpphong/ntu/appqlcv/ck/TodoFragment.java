package dpphong.ntu.appqlcv.ck;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
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

    private ChipGroup chipGroupLists;
    private final List<TaskList> taskLists = new ArrayList<>();
    private String listFilterId = null;

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
        chipGroupLists = view.findViewById(R.id.chip_group_lists);
        ImageButton btnOpenSearch = view.findViewById(R.id.btn_open_search);

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
        loadLists();

        // Mở màn hình tìm kiếm & lọc nâng cao
        btnOpenSearch.setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .replace(R.id.calendar_container, new SearchFragment())
                .addToBackStack(null)
                .commit());

        // Xử lý sự kiện khi ấn nút (+)
        fabAddTodo.setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .replace(R.id.calendar_container, new AddTodoFragment())
                .addToBackStack(null)
                .commit());

        return view;
    }

    /**
     * Tải danh sách của người dùng và tạo các chip lọc động.
     */
    private void loadLists() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        Query query = FirebaseDatabase.getInstance().getReference("Lists")
                .orderByChild("userId").equalTo(currentUser.getUid());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskLists.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    TaskList taskList = child.getValue(TaskList.class);
                    if (taskList != null) {
                        if (taskList.getId() == null) taskList.setId(child.getKey());
                        taskLists.add(taskList);
                    }
                }
                buildListChips();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi tải danh sách: " + error.getMessage());
            }
        });
    }

    private void buildListChips() {
        if (!isAdded() || chipGroupLists == null) return;

        chipGroupLists.setOnCheckedStateChangeListener(null);

        // Giữ lại chip "Mọi danh sách" có sẵn, xóa các chip động cũ
        while (chipGroupLists.getChildCount() > 1) {
            chipGroupLists.removeViewAt(1);
        }

        for (TaskList taskList : taskLists) {
            Chip chip = new Chip(requireContext());
            chip.setText(taskList.getName() != null ? taskList.getName() : "Danh sách");
            chip.setTag(taskList.getId());
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            chip.setTextSize(12f);
            chip.setTypeface(null, android.graphics.Typeface.BOLD);
            chip.setTextColor(ContextCompat.getColorStateList(requireContext(), R.color.chip_text_color));
            chip.setChipBackgroundColorResource(R.color.chip_bg_color);
            chip.setChipCornerRadius(20f * getResources().getDisplayMetrics().density);
            chip.setChipMinHeight(34f * getResources().getDisplayMetrics().density);
            chip.setEnsureMinTouchTargetSize(false);
            chip.setChipStrokeWidth(0f);
            chip.setId(View.generateViewId());

            if (listFilterId != null && listFilterId.equals(taskList.getId())) {
                chip.setChecked(true);
            }

            chipGroupLists.addView(chip);
        }

        chipGroupLists.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int checkedId = group.getCheckedChipId();
            if (checkedId == View.NO_ID || checkedId == R.id.chip_list_all) {
                listFilterId = null;
            } else {
                View checkedChip = group.findViewById(checkedId);
                listFilterId = checkedChip != null && checkedChip.getTag() != null
                        ? checkedChip.getTag().toString()
                        : null;
            }
            renderTasks();
        });

        // Nếu danh sách đang lọc bị xóa, quay về "Mọi danh sách"
        if (listFilterId != null) {
            boolean stillExists = false;
            int chipId = View.NO_ID;
            for (int i = 0; i < chipGroupLists.getChildCount(); i++) {
                View child = chipGroupLists.getChildAt(i);
                if (child.getTag() != null && listFilterId.equals(child.getTag().toString())) {
                    stillExists = true;
                    chipId = child.getId();
                    break;
                }
            }
            if (stillExists) {
                chipGroupLists.check(chipId);
            } else {
                listFilterId = null;
                chipGroupLists.check(R.id.chip_list_all);
            }
        } else {
            chipGroupLists.check(R.id.chip_list_all);
        }
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

                // Cập nhật bộ nhớ đệm cho widget màn hình chính
                if (isAdded() && getContext() != null) {
                    TaskCache.saveToday(getContext(), todayTasks);
                    TodayWidgetProvider.updateWidget(getContext());
                }
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
            if (listFilterId != null && !listFilterId.equals(task.getListId())) continue;
            if (filterMode == 1 && task.isCompleted()) continue;
            if (filterMode == 2 && !task.isCompleted()) continue;
            display.add(task);
        }
        taskAdapter.updateList(display);

        boolean isEmpty = display.isEmpty();
        layoutEmptyTodo.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvTodoToday.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (isEmpty) {
            if (listFilterId != null && total > 0) {
                tvEmptyTitle.setText("Danh sách này chưa có việc hôm nay");
                tvEmptyMessage.setText("Chọn danh sách khác hoặc thêm việc mới nhé");
            } else if (total == 0) {
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
