package dpphong.ntu.appqlcv.ck;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Thống kê công việc, thời gian tập trung, thói quen và nhật ký.
 */
public class StatsFragment extends Fragment {

    private TextView tvRate, tvSummary, tvHigh, tvMedium, tvLow, tvFocus, tvHabits, tvDiary;
    private LinearProgressIndicator progressRate, progressHigh, progressMedium, progressLow;
    private BarChartView barChart;
    private LinearLayout containerLists;

    private final List<Task> tasks = new ArrayList<>();
    private int focusMinutesToday = 0;
    private int focusSessionsToday = 0;
    private int habitDone = 0;
    private int habitTotal = 0;
    private int diaryCount = 0;
    private final Set<String> habitsDoneToday = new HashSet<>();
    private final Set<String> myHabitIds = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        tvRate = view.findViewById(R.id.tv_stats_rate);
        tvSummary = view.findViewById(R.id.tv_stats_summary);
        tvHigh = view.findViewById(R.id.tv_stats_priority_high);
        tvMedium = view.findViewById(R.id.tv_stats_priority_medium);
        tvLow = view.findViewById(R.id.tv_stats_priority_low);
        tvFocus = view.findViewById(R.id.tv_stats_focus);
        tvHabits = view.findViewById(R.id.tv_stats_habits);
        tvDiary = view.findViewById(R.id.tv_stats_diary);
        progressRate = view.findViewById(R.id.progress_stats_rate);
        progressHigh = view.findViewById(R.id.progress_stats_high);
        progressMedium = view.findViewById(R.id.progress_stats_medium);
        progressLow = view.findViewById(R.id.progress_stats_low);
        barChart = view.findViewById(R.id.bar_chart_week);
        containerLists = view.findViewById(R.id.container_stats_lists);

        ImageButton btnBack = view.findViewById(R.id.btn_back_stats);
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        loadTasks();
        loadFocus();
        loadHabits();
        loadDiaryCount();
        return view;
    }

    private void loadTasks() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
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
                renderTasks();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void renderTasks() {
        if (!isAdded()) return;

        int total = tasks.size();
        int completed = 0;
        int high = 0, medium = 0, low = 0;
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
        int overdue = 0;

        Map<String, Integer> listCounts = new HashMap<>();

        for (Task task : tasks) {
            if (task.isCompleted()) completed++;
            if (!task.isCompleted() && task.getDate() != null && task.getDate().compareTo(today) < 0) {
                overdue++;
            }

            String priority = task.getPriority() != null ? task.getPriority() : "Vừa";
            switch (priority) {
                case "Cao": high++; break;
                case "Thấp": low++; break;
                default: medium++; break;
            }

            String listName = task.getListName() != null && !task.getListName().isEmpty()
                    ? task.getListName() : "Không có danh sách";
            Integer count = listCounts.get(listName);
            listCounts.put(listName, (count != null ? count : 0) + 1);
        }

        int percent = total == 0 ? 0 : Math.round(completed * 100f / total);
        tvRate.setText(percent + "%");
        progressRate.setProgressCompat(percent, true);
        tvSummary.setText(completed + "/" + total + " việc đã xong"
                + (overdue > 0 ? " • " + overdue + " việc quá hạn" : ""));

        int maxPriority = Math.max(1, Math.max(high, Math.max(medium, low)));
        tvHigh.setText("Cao: " + high + " việc");
        tvMedium.setText("Vừa: " + medium + " việc");
        tvLow.setText("Thấp: " + low + " việc");
        progressHigh.setProgressCompat(Math.round(high * 100f / maxPriority), true);
        progressMedium.setProgressCompat(Math.round(medium * 100f / maxPriority), true);
        progressLow.setProgressCompat(Math.round(low * 100f / maxPriority), true);

        renderWeekChart();
        renderLists(listCounts);
    }

    private void renderWeekChart() {
        String[] labels = new String[7];
        int[] totals = new int[7];
        int[] completed = new int[7];

        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat labelFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());

        Calendar day = Calendar.getInstance();
        day.add(Calendar.DAY_OF_MONTH, -6);

        for (int i = 0; i < 7; i++) {
            String key = keyFormat.format(day.getTime());
            labels[i] = labelFormat.format(day.getTime());
            for (Task task : tasks) {
                if (task.getDate() != null && task.getDate().equals(key)) {
                    totals[i]++;
                    if (task.isCompleted()) completed[i]++;
                }
            }
            day.add(Calendar.DAY_OF_MONTH, 1);
        }

        barChart.setData(labels, completed, totals);
    }

    private void renderLists(Map<String, Integer> counts) {
        containerLists.removeAllViews();
        if (counts.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("Chưa có công việc nào");
            empty.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextSecondary));
            empty.setTextSize(13f);
            containerLists.addView(empty);
            return;
        }

        int max = 1;
        for (Integer value : counts.values()) max = Math.max(max, value);

        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(0, dp(6), 0, dp(6));

            TextView label = new TextView(requireContext());
            label.setText(entry.getKey() + " • " + entry.getValue() + " việc");
            label.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextPrimary));
            label.setTextSize(13f);

            LinearProgressIndicator progress = new LinearProgressIndicator(requireContext());
            progress.setMax(100);
            progress.setProgressCompat(Math.round(entry.getValue() * 100f / max), false);
            progress.setTrackThickness(dp(5));
            progress.setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
            progress.setTrackColor(ContextCompat.getColor(requireContext(), R.color.colorSurfaceMuted));

            LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            progressParams.topMargin = dp(4);
            progress.setLayoutParams(progressParams);

            row.addView(label);
            row.addView(progress);
            containerLists.addView(row);
        }
    }

    private void loadFocus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Query query = FirebaseDatabase.getInstance().getReference("FocusSessions")
                .orderByChild("userId").equalTo(user.getUid());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                focusMinutesToday = 0;
                focusSessionsToday = 0;

                Calendar now = Calendar.getInstance();
                int today = now.get(Calendar.DAY_OF_YEAR);
                int year = now.get(Calendar.YEAR);

                for (DataSnapshot child : snapshot.getChildren()) {
                    FocusSession session = child.getValue(FocusSession.class);
                    if (session == null) continue;
                    Calendar created = Calendar.getInstance();
                    created.setTimeInMillis(session.getCreatedAt());
                    if (created.get(Calendar.DAY_OF_YEAR) == today
                            && created.get(Calendar.YEAR) == year) {
                        focusMinutesToday += session.getMinutes();
                        focusSessionsToday++;
                    }
                }
                renderFocus();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void renderFocus() {
        if (!isAdded() || tvFocus == null) return;
        tvFocus.setText("⏱ Tập trung hôm nay: " + focusMinutesToday + " phút • "
                + focusSessionsToday + " phiên");
    }

    private void loadHabits() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        final Set<String> myHabits = new HashSet<>();
        FirebaseDatabase.getInstance().getReference("Habits")
                .orderByChild("userId").equalTo(user.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        myHabits.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            myHabits.add(child.getKey());
                        }
                        myHabitIds.clear();
                        myHabitIds.addAll(myHabits);
                        habitTotal = myHabits.size();
                        renderHabits();
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
                        habitsDoneToday.clear();
                        for (DataSnapshot habitNode : snapshot.getChildren()) {
                            if (habitNode.hasChild(today)) habitsDoneToday.add(habitNode.getKey());
                        }
                        renderHabits();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void renderHabits() {
        if (!isAdded() || tvHabits == null) return;
        habitDone = 0;
        for (String habitId : habitsDoneToday) {
            if (habitId != null && habitTotal > 0 && myHabitIds.contains(habitId)) habitDone++;
        }
        tvHabits.setText("✅ Thói quen hôm nay: " + habitDone + "/" + habitTotal);
    }

    private void loadDiaryCount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseDatabase.getInstance().getReference("Diaries")
                .orderByChild("userId").equalTo(user.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        diaryCount = (int) snapshot.getChildrenCount();
                        if (isAdded() && tvDiary != null) {
                            tvDiary.setText("📖 Nhật ký: " + diaryCount + " bài viết");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
