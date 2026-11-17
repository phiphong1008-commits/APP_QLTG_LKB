package dpphong.ntu.appqlcv.ck;

import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    public interface Listener {
        void onToggle(Habit habit);

        void onDelete(Habit habit);
    }

    private final List<Habit> habits = new ArrayList<>();
    private Map<String, Set<String>> logs;
    private final Listener listener;

    public HabitAdapter(Listener listener) {
        this.listener = listener;
    }

    public void update(List<Habit> newHabits, Map<String, Set<String>> newLogs) {
        habits.clear();
        if (newHabits != null) habits.addAll(newHabits);
        logs = newLogs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habits.get(position);
        holder.tvName.setText(habit.getName() != null ? habit.getName() : "Thói quen");
        holder.tvEmoji.setText(habit.getEmoji() != null ? habit.getEmoji() : "✅");

        Set<String> dates = getDates(habit.getId());
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Calendar.getInstance().getTime());

        boolean doneToday = dates.contains(today);
        holder.tvStatus.setText(doneToday ? "✅" : "○");
        holder.tvStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(),
                doneToday ? R.color.colorSuccess : R.color.colorTextHint));

        int streak = calculateStreak(dates);
        if (streak > 0) {
            holder.tvStreak.setText("🔥 " + streak + " ngày liên tiếp");
            holder.tvStreak.setTextColor(ContextCompat.getColor(holder.itemView.getContext(),
                    R.color.colorWarning));
        } else {
            holder.tvStreak.setText("Chưa có chuỗi • chạm để hoàn thành");
            holder.tvStreak.setTextColor(ContextCompat.getColor(holder.itemView.getContext(),
                    R.color.colorTextSecondary));
        }

        renderWeek(holder, dates);

        holder.itemView.setOnClickListener(v -> listener.onToggle(habit));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onDelete(habit);
            return true;
        });
    }

    private Set<String> getDates(String habitId) {
        if (logs == null || habitId == null) return new HashSet<>();
        Set<String> dates = logs.get(habitId);
        return dates != null ? dates : new HashSet<>();
    }

    private void renderWeek(HabitViewHolder holder, Set<String> dates) {
        holder.layoutDays.removeAllViews();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String[] labels = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        int diff = (calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7;
        calendar.add(Calendar.DAY_OF_MONTH, -diff);

        for (int i = 0; i < 7; i++) {
            String date = format.format(calendar.getTime());
            boolean done = dates.contains(date);

            TextView dot = new TextView(holder.itemView.getContext());
            dot.setText(labels[i]);
            dot.setTextSize(9f);
            dot.setTypeface(null, Typeface.BOLD);
            dot.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(holder, 22), dp(holder, 22));
            params.rightMargin = dp(holder, 4);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(done ? R.drawable.bg_pill_low : R.drawable.bg_pill_neutral);
            dot.setTextColor(ContextCompat.getColor(holder.itemView.getContext(),
                    done ? R.color.colorSuccess : R.color.colorTextHint));

            holder.layoutDays.addView(dot);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private int calculateStreak(Set<String> dates) {
        if (dates == null || dates.isEmpty()) return 0;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cursor = Calendar.getInstance();

        String today = format.format(cursor.getTime());
        if (!dates.contains(today)) {
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

    private int dp(HabitViewHolder holder, int value) {
        return Math.round(value * holder.itemView.getResources().getDisplayMetrics().density);
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvName, tvStreak, tvStatus;
        LinearLayout layoutDays;

        HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tv_habit_emoji);
            tvName = itemView.findViewById(R.id.tv_habit_name);
            tvStreak = itemView.findViewById(R.id.tv_habit_streak);
            tvStatus = itemView.findViewById(R.id.tv_habit_status);
            layoutDays = itemView.findViewById(R.id.layout_habit_days);
        }
    }
}
