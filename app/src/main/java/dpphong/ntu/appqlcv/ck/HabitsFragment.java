package dpphong.ntu.appqlcv.ck;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Quản lý thói quen, chuỗi ngày và lịch sử hoàn thành (HabitLogs).
 */
public class HabitsFragment extends Fragment {

    private RecyclerView rvHabits;
    private View layoutEmpty;
    private android.widget.TextView tvSubtitle;
    private HabitAdapter adapter;

    private final List<Habit> habits = new ArrayList<>();
    private final Map<String, Set<String>> logs = new HashMap<>();
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_habits, container, false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) userId = user.getUid();

        rvHabits = view.findViewById(R.id.rv_habits);
        layoutEmpty = view.findViewById(R.id.layout_empty_habits);
        tvSubtitle = view.findViewById(R.id.tv_habit_subtitle);
        ImageButton btnBack = view.findViewById(R.id.btn_back_habits);
        ImageButton btnAdd = view.findViewById(R.id.btn_add_habit);

        adapter = new HabitAdapter(new HabitAdapter.Listener() {
            @Override
            public void onToggle(Habit habit) {
                toggleHabit(habit);
            }

            @Override
            public void onDelete(Habit habit) {
                confirmDelete(habit);
            }
        });

        rvHabits.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvHabits.setAdapter(adapter);

        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });
        btnAdd.setOnClickListener(v -> showAddHabitDialog());

        loadHabits();
        loadLogs();
        return view;
    }

    private void loadHabits() {
        if (userId == null) {
            render();
            return;
        }

        Query query = FirebaseDatabase.getInstance().getReference("Habits")
                .orderByChild("userId").equalTo(userId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                habits.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Habit habit = child.getValue(Habit.class);
                    if (habit != null) {
                        if (habit.getId() == null) habit.setId(child.getKey());
                        habits.add(habit);
                    }
                }
                render();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadLogs() {
        FirebaseDatabase.getInstance().getReference("HabitLogs")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        logs.clear();
                        for (DataSnapshot habitNode : snapshot.getChildren()) {
                            String habitId = habitNode.getKey();
                            Set<String> dates = new HashSet<>();
                            for (DataSnapshot dateNode : habitNode.getChildren()) {
                                if (dateNode.getKey() != null) dates.add(dateNode.getKey());
                            }
                            if (habitId != null) logs.put(habitId, dates);
                        }
                        render();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void render() {
        if (!isAdded()) return;
        adapter.update(habits, logs);
        boolean empty = habits.isEmpty();
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvHabits.setVisibility(empty ? View.GONE : View.VISIBLE);

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        int doneToday = 0;
        for (Habit habit : habits) {
            Set<String> dates = logs.get(habit.getId());
            if (dates != null && dates.contains(today)) doneToday++;
        }
        tvSubtitle.setText(doneToday + "/" + habits.size() + " thói quen hôm nay");
    }

    private void toggleHabit(Habit habit) {
        if (habit.getId() == null) return;
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Set<String> dates = logs.get(habit.getId());
        boolean done = dates != null && dates.contains(today);

        if (done) {
            FirebaseDatabase.getInstance().getReference("HabitLogs")
                    .child(habit.getId()).child(today).removeValue();
        } else {
            FirebaseDatabase.getInstance().getReference("HabitLogs")
                    .child(habit.getId()).child(today).setValue(true);
            Toast.makeText(getContext(), "Tuyệt! Đã giữ chuỗi " + habit.getName(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddHabitDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_habit, null, false);

        EditText edtName = dialogView.findViewById(R.id.edt_habit_name);
        ChipGroup chipEmoji = dialogView.findViewById(R.id.chip_group_habit_emoji);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Thêm thói quen")
                .setView(dialogView)
                .setPositiveButton("Lưu", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String name = edtName.getText() != null ? edtName.getText().toString().trim() : "";
                    if (TextUtils.isEmpty(name)) {
                        edtName.setError("Nhập tên thói quen");
                        return;
                    }
                    saveHabit(name, getSelectedEmoji(chipEmoji));
                    dialog.dismiss();
                }));

        dialog.show();
    }

    private String getSelectedEmoji(ChipGroup group) {
        int id = group.getCheckedChipId();
        Chip chip = group.findViewById(id);
        return chip != null && chip.getText() != null ? chip.getText().toString() : "✅";
    }

    private void saveHabit(String name, String emoji) {
        if (userId == null) return;
        String id = FirebaseDatabase.getInstance().getReference("Habits").push().getKey();
        if (id == null) return;

        Habit habit = new Habit();
        habit.setId(id);
        habit.setUserId(userId);
        habit.setName(name);
        habit.setEmoji(emoji);
        habit.setColor("#4C6FFF");
        habit.setCreatedAt(System.currentTimeMillis());

        FirebaseDatabase.getInstance().getReference("Habits").child(id).setValue(habit)
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Không lưu được: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void confirmDelete(Habit habit) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa thói quen")
                .setMessage("Xóa '" + habit.getName() + "' và toàn bộ lịch sử chuỗi?")
                .setIcon(R.drawable.ic_delete)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    if (habit.getId() == null) return;
                    FirebaseDatabase.getInstance().getReference("Habits")
                            .child(habit.getId()).removeValue();
                    FirebaseDatabase.getInstance().getReference("HabitLogs")
                            .child(habit.getId()).removeValue();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
