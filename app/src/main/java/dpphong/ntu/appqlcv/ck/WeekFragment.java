package dpphong.ntu.appqlcv.ck;

import android.os.Bundle;
import android.util.Log;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Lịch tuần dạng agenda: 7 ngày trong tuần, chọn ngày để xem việc.
 * Nhấn giữ công việc để dời sang ngày khác (thao tác trong TaskAdapter).
 */
public class WeekFragment extends Fragment {

    private LinearLayout layoutDays;
    private RecyclerView rvTasks;
    private View layoutEmpty;
    private TextView tvRange, tvSelected;
    private FloatingActionButton fabAdd;
    private TaskAdapter adapter;

    private final List<Task> allTasks = new ArrayList<>();
    private final List<DayCell> dayCells = new ArrayList<>();

    private Calendar weekStart;
    private String selectedDate;
    private final SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_week, container, false);

        layoutDays = view.findViewById(R.id.layout_week_days);
        rvTasks = view.findViewById(R.id.rv_week_tasks);
        layoutEmpty = view.findViewById(R.id.layout_week_empty);
        tvRange = view.findViewById(R.id.tv_week_range);
        tvSelected = view.findViewById(R.id.tv_week_selected);
        fabAdd = view.findViewById(R.id.fab_add_week_task);
        ImageButton btnBack = view.findViewById(R.id.btn_back_week);
        ImageButton btnPrev = view.findViewById(R.id.btn_prev_week);
        ImageButton btnNext = view.findViewById(R.id.btn_next_week);

        adapter = new TaskAdapter(new ArrayList<>());
        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTasks.setAdapter(adapter);

        weekStart = startOfCurrentWeek();
        selectedDate = dbFormat.format(Calendar.getInstance().getTime());

        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        btnPrev.setOnClickListener(v -> moveWeek(-7));
        btnNext.setOnClickListener(v -> moveWeek(7));

        fabAdd.setOnClickListener(v -> {
            AddTodoFragment addFragment = new AddTodoFragment();
            Bundle args = new Bundle();
            args.putString("TASK_DATE", selectedDate);
            addFragment.setArguments(args);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.calendar_container, addFragment)
                    .addToBackStack(null)
                    .commit();
        });

        buildDayCells();
        loadTasks();
        return view;
    }

    private Calendar startOfCurrentWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        int diff = (calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7;
        calendar.add(Calendar.DAY_OF_MONTH, -diff);
        return calendar;
    }

    private void moveWeek(int days) {
        weekStart.add(Calendar.DAY_OF_MONTH, days);
        selectedDate = dbFormat.format(weekStart.getTime());
        buildDayCells();
        render();
    }

    private void buildDayCells() {
        if (layoutDays == null) return;
        layoutDays.removeAllViews();
        dayCells.clear();

        String[] labels = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};

        Calendar cursor = (Calendar) weekStart.clone();
        for (int i = 0; i < 7; i++) {
            final String date = dbFormat.format(cursor.getTime());
            final int dayOfMonth = cursor.get(Calendar.DAY_OF_MONTH);

            LinearLayout cell = new LinearLayout(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            cell.setLayoutParams(params);
            cell.setGravity(Gravity.CENTER);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setPadding(0, 10, 0, 10);

            TextView tvLabel = new TextView(requireContext());
            tvLabel.setText(labels[i]);
            tvLabel.setTextSize(11f);
            tvLabel.setGravity(Gravity.CENTER);
            tvLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextSecondary));

            TextView tvNumber = new TextView(requireContext());
            tvNumber.setText(String.valueOf(dayOfMonth));
            tvNumber.setTextSize(15f);
            tvNumber.setTypeface(null, android.graphics.Typeface.BOLD);
            tvNumber.setGravity(Gravity.CENTER);
            tvNumber.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            View dot = new View(requireContext());
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dp(5), dp(5));
            dotParams.topMargin = dp(4);
            dot.setLayoutParams(dotParams);
            dot.setBackgroundResource(R.drawable.bg_color_dot);
            dot.setVisibility(View.GONE);

            cell.addView(tvLabel);
            cell.addView(tvNumber);
            cell.addView(dot);

            cell.setOnClickListener(v -> {
                selectedDate = date;
                updateDayStyles();
                render();
            });

            layoutDays.addView(cell);
            dayCells.add(new DayCell(date, cell, tvLabel, tvNumber, dot));
            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }

        Calendar end = (Calendar) weekStart.clone();
        end.add(Calendar.DAY_OF_MONTH, 6);
        SimpleDateFormat rangeFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
        tvRange.setText("Tuần " + rangeFormat.format(weekStart.getTime())
                + " - " + rangeFormat.format(end.getTime()));

        updateDayStyles();
    }

    private void updateDayStyles() {
        for (DayCell dayCell : dayCells) {
            boolean selected = dayCell.date.equals(selectedDate);
            dayCell.container.setBackgroundResource(selected ? R.drawable.bg_pill_soft : 0);
            int color = ContextCompat.getColor(requireContext(),
                    selected ? R.color.colorPrimary : R.color.colorTextPrimary);
            dayCell.number.setTextColor(color);
            dayCell.label.setTextColor(ContextCompat.getColor(requireContext(),
                    selected ? R.color.colorPrimary : R.color.colorTextSecondary));
        }
    }

    private void loadTasks() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            render();
            return;
        }

        Query query = FirebaseDatabase.getInstance().getReference("Tasks")
                .orderByChild("userId").equalTo(user.getUid());

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
                Log.e("WeekFragment", "Lỗi tải tuần: " + error.getMessage());
            }
        });
    }

    private void render() {
        if (!isAdded() || adapter == null) return;

        Set<String> datesWithTasks = new HashSet<>();
        List<Task> selected = new ArrayList<>();
        for (Task task : allTasks) {
            if (task.getDate() != null) {
                datesWithTasks.add(task.getDate());
                if (task.getDate().equals(selectedDate)) selected.add(task);
            }
        }

        for (DayCell cell : dayCells) {
            cell.dot.setVisibility(datesWithTasks.contains(cell.date) ? View.VISIBLE : View.GONE);
        }

        selected.sort((a, b) -> {
            String ta = a.getTime() != null ? a.getTime() : "";
            String tb = b.getTime() != null ? b.getTime() : "";
            return ta.compareTo(tb);
        });

        adapter.updateList(selected);
        layoutEmpty.setVisibility(selected.isEmpty() ? View.VISIBLE : View.GONE);
        rvTasks.setVisibility(selected.isEmpty() ? View.GONE : View.VISIBLE);

        try {
            Date date = dbFormat.parse(selectedDate);
            if (date != null) {
                SimpleDateFormat out = new SimpleDateFormat("EEEE, dd/MM", new Locale("vi", "VN"));
                String text = out.format(date);
                text = Character.toUpperCase(text.charAt(0)) + text.substring(1);
                tvSelected.setText(text + " • " + selected.size() + " việc");
            }
        } catch (Exception ignored) {
            tvSelected.setText(selectedDate + " • " + selected.size() + " việc");
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static class DayCell {
        final String date;
        final LinearLayout container;
        final TextView label;
        final TextView number;
        final View dot;

        DayCell(String date, LinearLayout container, TextView label, TextView number, View dot) {
            this.date = date;
            this.container = container;
            this.label = label;
            this.number = number;
            this.dot = dot;
        }
    }
}
