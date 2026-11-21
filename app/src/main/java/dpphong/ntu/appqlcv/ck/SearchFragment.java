package dpphong.ntu.appqlcv.ck;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
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

public class SearchFragment extends Fragment {

    private EditText edtSearch;
    private RecyclerView rvSearch;
    private View layoutEmpty;
    private TextView tvCount;
    private ChipGroup chipStatus, chipPriority, chipDate;

    private TaskAdapter adapter;
    private final List<Task> allTasks = new ArrayList<>();

    private int statusMode = 0; // 0 tất cả, 1 chưa xong, 2 đã xong
    private int priorityMode = 0; // 0 tất cả, 1 cao, 2 vừa, 3 thấp
    private int dateMode = 0; // 0 mọi lúc, 1 hôm nay, 2 bảy ngày tới

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        edtSearch = view.findViewById(R.id.edt_search);
        rvSearch = view.findViewById(R.id.rv_search);
        layoutEmpty = view.findViewById(R.id.layout_empty_search);
        tvCount = view.findViewById(R.id.tv_search_count);
        chipStatus = view.findViewById(R.id.chip_group_search_status);
        chipPriority = view.findViewById(R.id.chip_group_search_priority);
        chipDate = view.findViewById(R.id.chip_group_search_date);

        ImageButton btnBack = view.findViewById(R.id.btn_back_search);
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        adapter = new TaskAdapter(new ArrayList<>());
        rvSearch.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSearch.setAdapter(adapter);

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                render();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        chipStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int id = group.getCheckedChipId();
            if (id == R.id.chip_search_pending) statusMode = 1;
            else if (id == R.id.chip_search_done) statusMode = 2;
            else statusMode = 0;
            render();
        });

        chipPriority.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int id = group.getCheckedChipId();
            if (id == R.id.chip_priority_high) priorityMode = 1;
            else if (id == R.id.chip_priority_medium) priorityMode = 2;
            else if (id == R.id.chip_priority_low) priorityMode = 3;
            else priorityMode = 0;
            render();
        });

        chipDate.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int id = group.getCheckedChipId();
            if (id == R.id.chip_date_today) dateMode = 1;
            else if (id == R.id.chip_date_week) dateMode = 2;
            else dateMode = 0;
            render();
        });

        loadTasks();
        return view;
    }

    private void loadTasks() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            render();
            return;
        }

        Query query = FirebaseDatabase.getInstance().getReference("Tasks")
                .orderByChild("userId").equalTo(currentUser.getUid());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allTasks.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Task task = child.getValue(Task.class);
                    if (task != null) allTasks.add(task);
                }
                render();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                render();
            }
        });
    }

    private void render() {
        if (!isAdded()) return;

        String keyword = edtSearch.getText().toString().trim().toLowerCase(Locale.getDefault());
        String today = formatDate(Calendar.getInstance().getTime());
        String weekEnd = formatDate(addDays(new Date(), 7));

        List<Task> result = new ArrayList<>();
        for (Task task : allTasks) {
            if (!matchesKeyword(task, keyword)) continue;
            if (statusMode == 1 && task.isCompleted()) continue;
            if (statusMode == 2 && !task.isCompleted()) continue;
            if (!matchesPriority(task)) continue;
            if (!matchesDate(task, today, weekEnd)) continue;
            result.add(task);
        }

        adapter.updateList(result);
        tvCount.setText(result.size() + " kết quả");

        boolean empty = result.isEmpty();
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvSearch.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private boolean matchesKeyword(Task task, String keyword) {
        if (TextUtils.isEmpty(keyword)) return true;

        String title = task.getTitle() != null ? task.getTitle().toLowerCase(Locale.getDefault()) : "";
        String desc = task.getDescription() != null ? task.getDescription().toLowerCase(Locale.getDefault()) : "";
        String list = task.getListName() != null ? task.getListName().toLowerCase(Locale.getDefault()) : "";
        return title.contains(keyword) || desc.contains(keyword) || list.contains(keyword);
    }

    private boolean matchesPriority(Task task) {
        if (priorityMode == 0) return true;
        String priority = task.getPriority() != null ? task.getPriority() : "Vừa";
        if (priorityMode == 1) return "Cao".equals(priority);
        if (priorityMode == 3) return "Thấp".equals(priority);
        return "Vừa".equals(priority);
    }

    private boolean matchesDate(Task task, String today, String weekEnd) {
        if (dateMode == 0) return true;
        String date = task.getDate();
        if (TextUtils.isEmpty(date)) return false;
        if (dateMode == 1) return date.equals(today);
        return date.compareTo(today) >= 0 && date.compareTo(weekEnd) <= 0;
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
    }

    private Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }
}
