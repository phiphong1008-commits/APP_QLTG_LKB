package dpphong.ntu.appqlcv.ck;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Trợ lý AI cục bộ: phân tích công việc, thói quen, nhật ký và thời gian tập trung
 * bằng quy tắc trên máy (không gọi dịch vụ AI bên ngoài).
 */
public class AssistantFragment extends Fragment {

    private TextView tvInsight;
    private ChipGroup chipTone;

    private final List<Task> tasks = new ArrayList<>();
    private int focusMinutesToday = 0;
    private int habitDone = 0;
    private int habitTotal = 0;
    private int diaryCount = 0;
    private int bestStreak = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assistant, container, false);

        tvInsight = view.findViewById(R.id.tv_assistant_insight);
        chipTone = view.findViewById(R.id.chip_group_ai_tone);
        ImageButton btnBack = view.findViewById(R.id.btn_back_assistant);
        ImageButton btnRefresh = view.findViewById(R.id.btn_assistant_refresh);

        String tone = Prefs.getAiTone(requireContext());
        if ("strict".equals(tone)) chipTone.check(R.id.chip_tone_strict);
        else if ("motivate".equals(tone)) chipTone.check(R.id.chip_tone_motivate);
        else chipTone.check(R.id.chip_tone_friendly);

        chipTone.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int id = group.getCheckedChipId();
            String value = "friendly";
            if (id == R.id.chip_tone_strict) value = "strict";
            else if (id == R.id.chip_tone_motivate) value = "motivate";
            Prefs.setAiTone(requireContext(), value);
            renderInsight();
        });

        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        btnRefresh.setOnClickListener(v -> {
            renderInsight();
            android.widget.Toast.makeText(getContext(), "Đã phân tích lại",
                    android.widget.Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.btn_assistant_quick_add).setOnClickListener(v ->
                new QuickAddDialogFragment().show(getParentFragmentManager(), "quick_add_ai"));

        loadTasks();
        loadFocus();
        loadDiaries();
        loadHabits();
        return view;
    }

    private FirebaseUser user() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    private void loadTasks() {
        FirebaseUser user = user();
        if (user == null) return;
        Query query = FirebaseDatabase.getInstance().getReference("Tasks")
                .orderByChild("userId").equalTo(user.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tasks.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Task task = child.getValue(Task.class);
                    if (task != null) tasks.add(task);
                }
                renderInsight();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadFocus() {
        FirebaseUser user = user();
        if (user == null) return;
        Query query = FirebaseDatabase.getInstance().getReference("FocusSessions")
                .orderByChild("userId").equalTo(user.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                focusMinutesToday = 0;
                Calendar now = Calendar.getInstance();
                for (DataSnapshot child : snapshot.getChildren()) {
                    FocusSession session = child.getValue(FocusSession.class);
                    if (session == null) continue;
                    Calendar created = Calendar.getInstance();
                    created.setTimeInMillis(session.getCreatedAt());
                    if (created.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
                            && created.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
                        focusMinutesToday += session.getMinutes();
                    }
                }
                renderInsight();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadDiaries() {
        FirebaseUser user = user();
        if (user == null) return;
        FirebaseDatabase.getInstance().getReference("Diaries")
                .orderByChild("userId").equalTo(user.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        diaryCount = (int) snapshot.getChildrenCount();
                        renderInsight();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void loadHabits() {
        FirebaseUser user = user();
        if (user == null) return;

        final Set<String> myHabits = new HashSet<>();
        FirebaseDatabase.getInstance().getReference("Habits")
                .orderByChild("userId").equalTo(user.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        myHabits.clear();
                        for (DataSnapshot child : snapshot.getChildren()) myHabits.add(child.getKey());
                        habitTotal = myHabits.size();
                        renderInsight();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        FirebaseDatabase.getInstance().getReference("HabitLogs")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(Calendar.getInstance().getTime());
                        habitDone = 0;
                        bestStreak = 0;
                        Map<String, Set<String>> logs = new HashMap<>();

                        for (DataSnapshot habitNode : snapshot.getChildren()) {
                            Set<String> dates = new HashSet<>();
                            for (DataSnapshot dateNode : habitNode.getChildren()) {
                                if (dateNode.getKey() != null) dates.add(dateNode.getKey());
                            }
                            logs.put(habitNode.getKey(), dates);
                            if (myHabits.contains(habitNode.getKey()) && dates.contains(today)) {
                                habitDone++;
                            }
                        }
                        for (String habitId : myHabits) {
                            bestStreak = Math.max(bestStreak, streakOf(logs.get(habitId)));
                        }
                        renderInsight();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private int streakOf(Set<String> dates) {
        if (dates == null || dates.isEmpty()) return 0;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cursor = Calendar.getInstance();
        if (!dates.contains(format.format(cursor.getTime()))) {
            cursor.add(Calendar.DAY_OF_MONTH, -1);
        }
        int streak = 0;
        while (streak < 3650) {
            String date = format.format(cursor.getTime());
            if (!dates.contains(date)) break;
            streak++;
            cursor.add(Calendar.DAY_OF_MONTH, -1);
        }
        return streak;
    }

    private void renderInsight() {
        if (!isAdded() || tvInsight == null) return;

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Calendar.getInstance().getTime());

        List<Task> pending = new ArrayList<>();
        int overdue = 0;
        int completedToday = 0;
        for (Task task : tasks) {
            if (!task.isCompleted()) {
                pending.add(task);
                if (task.getDate() != null && task.getDate().compareTo(today) < 0) overdue++;
            } else if (task.getDate() != null && task.getDate().equals(today)) {
                completedToday++;
            }
        }

        pending.sort((a, b) -> {
            int rankA = priorityRank(a.getPriority());
            int rankB = priorityRank(b.getPriority());
            if (rankA != rankB) return rankA - rankB;
            String da = a.getDate() != null ? a.getDate() : "9999-12-31";
            String db = b.getDate() != null ? b.getDate() : "9999-12-31";
            return da.compareTo(db);
        });

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = hour < 11 ? "Chào buổi sáng ☀️"
                : hour < 18 ? "Chào buổi chiều 🌤" : "Chào buổi tối 🌙";

        String tone = Prefs.getAiTone(requireContext());
        StringBuilder builder = new StringBuilder();
        builder.append(greeting).append("\n\n");

        builder.append("📋 Bạn có ").append(pending.size()).append(" việc chưa xong");
        if (overdue > 0) builder.append(", trong đó ").append(overdue).append(" việc quá hạn");
        builder.append(".\n");

        if (!pending.isEmpty()) {
            builder.append("\n🎯 Nên ưu tiên:\n");
            int limit = Math.min(3, pending.size());
            for (int i = 0; i < limit; i++) {
                Task task = pending.get(i);
                builder.append(i + 1).append(". ").append(task.getTitle());
                if (task.getDate() != null) builder.append(" (").append(task.getDate()).append(")");
                builder.append(" • ").append(task.getPriority() != null ? task.getPriority() : "Vừa");
                builder.append("\n");
            }
        } else {
            builder.append("\n✅ Tuyệt vời, danh sách đang trống. Hãy lên kế hoạch cho ngày mai.\n");
        }

        builder.append("\n⏱ Tập trung hôm nay: ").append(focusMinutesToday).append(" phút");
        builder.append("\n💧 Thói quen: ").append(habitDone).append("/").append(habitTotal);
        if (bestStreak > 0) builder.append(" • chuỗi dài nhất ").append(bestStreak).append(" ngày");
        builder.append("\n📖 Nhật ký: ").append(diaryCount).append(" bài");
        if (completedToday > 0) builder.append("\n🎉 Hôm nay đã hoàn thành ").append(completedToday).append(" việc");

        builder.append("\n\n💡 ");
        if ("strict".equals(tone)) {
            builder.append(overdue > 0
                    ? "Bạn đang để " + overdue + " việc quá hạn. Chọn một việc và làm ngay trong 25 phút, đừng mở mạng xã hội trước."
                    : "Đừng thỏa mãn với danh sách trống. Đặt thêm một mục tiêu nhỏ cho hôm nay và hoàn thành trước khi nghỉ.");
        } else if ("motivate".equals(tone)) {
            builder.append("Mỗi việc hoàn thành là một bước tiến. Hãy bắt đầu với việc nhỏ nhất trong 5 phút, đà sẽ tự đến!");
        } else {
            builder.append(pending.isEmpty()
                    ? "Bạn đang làm rất tốt. Dành 5 phút viết nhật ký để ghi lại cảm xúc hôm nay nhé."
                    : "Bắt đầu với việc quan trọng nhất thôi. Một phiên 25 phút là đủ để tạo đà cho cả ngày.");
        }

        builder.append("\n\n(Trợ lý cục bộ • không gửi dữ liệu ra ngoài)");
        tvInsight.setText(builder.toString());
    }

    private int priorityRank(String priority) {
        if ("Cao".equals(priority)) return 0;
        if ("Thấp".equals(priority)) return 2;
        return 1;
    }
}
