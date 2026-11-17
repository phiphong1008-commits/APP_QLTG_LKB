package dpphong.ntu.appqlcv.ck;

import android.os.Bundle;
import android.view.Gravity;
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
 * Thành tích / gamification dựa trên dữ liệu thật của người dùng.
 */
public class AchievementsFragment extends Fragment {

    private LinearLayout container;
    private TextView tvCount, tvHint;

    private int completedTasks = 0;
    private int focusMinutes = 0;
    private int diaryCount = 0;
    private int habitCount = 0;
    private int bestStreak = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_achievements, container, false);
        this.container = view.findViewById(R.id.container_achievements);
        tvCount = view.findViewById(R.id.tv_achievement_count);
        tvHint = view.findViewById(R.id.tv_achievement_hint);

        ImageButton btnBack = view.findViewById(R.id.btn_back_achievements);
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

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
                completedTasks = 0;
                for (DataSnapshot child : snapshot.getChildren()) {
                    Task task = child.getValue(Task.class);
                    if (task != null && task.isCompleted()) completedTasks++;
                }
                render();
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
                focusMinutes = 0;
                for (DataSnapshot child : snapshot.getChildren()) {
                    FocusSession session = child.getValue(FocusSession.class);
                    if (session != null) focusMinutes += session.getMinutes();
                }
                render();
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
                        render();
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
                        habitCount = myHabits.size();
                        render();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        FirebaseDatabase.getInstance().getReference("HabitLogs")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String, Set<String>> logs = new HashMap<>();
                        for (DataSnapshot habitNode : snapshot.getChildren()) {
                            Set<String> dates = new HashSet<>();
                            for (DataSnapshot dateNode : habitNode.getChildren()) {
                                if (dateNode.getKey() != null) dates.add(dateNode.getKey());
                            }
                            logs.put(habitNode.getKey(), dates);
                        }
                        bestStreak = 0;
                        for (String habitId : myHabits) {
                            bestStreak = Math.max(bestStreak, streakOf(logs.get(habitId)));
                        }
                        render();
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
        String today = format.format(cursor.getTime());
        if (!dates.contains(today)) cursor.add(Calendar.DAY_OF_MONTH, -1);

        int streak = 0;
        while (streak < 3650) {
            String date = format.format(cursor.getTime());
            if (!dates.contains(date)) break;
            streak++;
            cursor.add(Calendar.DAY_OF_MONTH, -1);
        }
        return streak;
    }

    private void render() {
        if (!isAdded() || container == null) return;

        List<Badge> badges = new ArrayList<>();
        badges.add(new Badge("🌱", "Bước đầu tiên", "Hoàn thành công việc đầu tiên", completedTasks >= 1));
        badges.add(new Badge("🔥", "Siêng năng", "Hoàn thành 10 công việc", completedTasks >= 10));
        badges.add(new Badge("🏅", "Bậc thầy", "Hoàn thành 50 công việc", completedTasks >= 50));
        badges.add(new Badge("⏱", "Tập trung 60 phút", "Tích lũy 60 phút tập trung", focusMinutes >= 60));
        badges.add(new Badge("🎯", "Tập trung 300 phút", "Tích lũy 300 phút tập trung", focusMinutes >= 300));
        badges.add(new Badge("💧", "Người xây thói quen", "Tạo thói quen đầu tiên", habitCount >= 1));
        badges.add(new Badge("✨", "Chuỗi 3 ngày", "Giữ chuỗi thói quen 3 ngày", bestStreak >= 3));
        badges.add(new Badge("🌟", "Chuỗi 7 ngày", "Giữ chuỗi thói quen 7 ngày", bestStreak >= 7));
        badges.add(new Badge("📖", "Người ghi chép", "Viết bài nhật ký đầu tiên", diaryCount >= 1));
        badges.add(new Badge("✍️", "Nhà văn nhỏ", "Viết 10 bài nhật ký", diaryCount >= 10));

        int unlocked = 0;
        container.removeAllViews();
        for (Badge badge : badges) {
            if (badge.unlocked) unlocked++;
            container.addView(buildRow(badge));
        }

        tvCount.setText(unlocked + "/" + badges.size() + " thành tích");
        tvHint.setText(unlocked == 0
                ? "Hoàn thành công việc để mở khóa"
                : "Tuyệt vời! Tiếp tục giữ nhịp độ này nhé");
    }

    private View buildRow(Badge badge) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(12), dp(14), dp(12));
        row.setBackgroundResource(badge.unlocked ? R.drawable.bg_pill_soft : R.drawable.bg_pill_neutral);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(8);
        row.setLayoutParams(params);

        TextView emoji = new TextView(requireContext());
        emoji.setText(badge.emoji);
        emoji.setTextSize(24f);
        emoji.setGravity(Gravity.CENTER);
        emoji.setLayoutParams(new LinearLayout.LayoutParams(dp(44), dp(44)));

        LinearLayout texts = new LinearLayout(requireContext());
        texts.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        textParams.leftMargin = dp(10);
        texts.setLayoutParams(textParams);

        TextView title = new TextView(requireContext());
        title.setText(badge.title);
        title.setTextSize(14f);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextColor(ContextCompat.getColor(requireContext(),
                badge.unlocked ? R.color.colorTextPrimary : R.color.colorTextHint));

        TextView desc = new TextView(requireContext());
        desc.setText(badge.description);
        desc.setTextSize(12f);
        desc.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextSecondary));

        texts.addView(title);
        texts.addView(desc);

        TextView status = new TextView(requireContext());
        status.setText(badge.unlocked ? "Đã mở" : "🔒");
        status.setTextSize(11f);
        status.setTextColor(ContextCompat.getColor(requireContext(),
                badge.unlocked ? R.color.colorSuccess : R.color.colorTextHint));

        row.addView(emoji);
        row.addView(texts);
        row.addView(status);
        return row;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static class Badge {
        final String emoji;
        final String title;
        final String description;
        final boolean unlocked;

        Badge(String emoji, String title, String description, boolean unlocked) {
            this.emoji = emoji;
            this.title = title;
            this.description = description;
            this.unlocked = unlocked;
        }
    }
}
