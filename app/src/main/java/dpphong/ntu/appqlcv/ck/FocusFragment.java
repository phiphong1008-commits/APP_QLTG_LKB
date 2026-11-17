package dpphong.ntu.appqlcv.ck;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Đồng hồ Pomodoro, lưu phiên tập trung vào FocusSessions để thống kê thời gian.
 */
public class FocusFragment extends Fragment {

    private TextView tvTimer, tvMode, tvTodayTotal;
    private EditText edtTask;
    private MaterialButton btnStart;
    private ChipGroup chipDuration;
    private LinearLayout containerSessions;

    private CountDownTimer timer;
    private long totalMillis = 25 * 60 * 1000L;
    private long remainingMillis = totalMillis;
    private long startedAt;
    private boolean running = false;
    private boolean paused = false;

    private String taskId;
    private final List<FocusSession> todaySessions = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_focus, container, false);

        tvTimer = view.findViewById(R.id.tv_focus_timer);
        tvMode = view.findViewById(R.id.tv_focus_mode);
        tvTodayTotal = view.findViewById(R.id.tv_focus_today_total);
        edtTask = view.findViewById(R.id.edt_focus_task);
        btnStart = view.findViewById(R.id.btn_focus_start);
        chipDuration = view.findViewById(R.id.chip_group_focus_duration);
        containerSessions = view.findViewById(R.id.container_focus_sessions);
        ImageButton btnBack = view.findViewById(R.id.btn_back_focus);
        MaterialButton btnFinish = view.findViewById(R.id.btn_focus_finish);

        if (getArguments() != null) {
            taskId = getArguments().getString("TASK_ID");
            String title = getArguments().getString("TASK_TITLE");
            if (!TextUtils.isEmpty(title)) edtTask.setText(title);
        }

        btnBack.setOnClickListener(v -> {
            if (running) stopTimer();
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        chipDuration.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (running) return;
            int id = group.getCheckedChipId();
            int minutes = 25;
            if (id == R.id.chip_focus_15) minutes = 15;
            else if (id == R.id.chip_focus_45) minutes = 45;
            else if (id == R.id.chip_focus_60) minutes = 60;

            totalMillis = minutes * 60 * 1000L;
            remainingMillis = totalMillis;
            paused = false;
            updateTimerText();
            tvMode.setText("Sẵn sàng • " + minutes + " phút");
            btnStart.setText("Bắt đầu");
        });

        btnStart.setOnClickListener(v -> {
            if (running) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        btnFinish.setOnClickListener(v -> finishSession());

        updateTimerText();
        loadTodaySessions();
        return view;
    }

    private void startTimer() {
        if (remainingMillis <= 0) remainingMillis = totalMillis;
        if (!paused) {
            startedAt = System.currentTimeMillis();
        }
        running = true;
        paused = false;
        btnStart.setText("Tạm dừng");
        tvMode.setText("Đang tập trung...");

        timer = new CountDownTimer(remainingMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingMillis = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                remainingMillis = 0;
                running = false;
                updateTimerText();
                tvMode.setText("Hoàn thành! Nghỉ một chút nhé 🎉");
                btnStart.setText("Bắt đầu");
                logSession(Math.round(totalMillis / 60000f));
                remainingMillis = totalMillis;
                paused = false;
            }
        }.start();
    }

    private void pauseTimer() {
        if (timer != null) timer.cancel();
        running = false;
        paused = true;
        btnStart.setText("Tiếp tục");
        tvMode.setText("Đang tạm dừng");
    }

    private void stopTimer() {
        if (timer != null) timer.cancel();
        running = false;
    }

    private void finishSession() {
        long elapsed = totalMillis - remainingMillis;
        if (elapsed >= 60_000L) {
            logSession(Math.max(1, Math.round(elapsed / 60000f)));
        }
        stopTimer();
        remainingMillis = totalMillis;
        paused = false;
        updateTimerText();
        tvMode.setText("Sẵn sàng tập trung");
        btnStart.setText("Bắt đầu");
    }

    private void updateTimerText() {
        long seconds = Math.max(0, remainingMillis / 1000);
        long minutes = seconds / 60;
        long secs = seconds % 60;
        tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, secs));
    }

    private void logSession(int minutes) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || minutes <= 0) return;

        FocusSession session = new FocusSession();
        session.setUserId(user.getUid());
        session.setTaskId(taskId);
        session.setTaskTitle(edtTask.getText().toString().trim());
        session.setMinutes(minutes);
        session.setStartedAt(startedAt > 0 ? startedAt : System.currentTimeMillis());
        session.setCreatedAt(System.currentTimeMillis());

        String id = FirebaseDatabase.getInstance().getReference("FocusSessions").push().getKey();
        if (id == null) return;
        session.setId(id);

        FirebaseDatabase.getInstance().getReference("FocusSessions").child(id).setValue(session)
                .addOnSuccessListener(aVoid -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Đã ghi nhận " + minutes + " phút tập trung",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadTodaySessions() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Query query = FirebaseDatabase.getInstance().getReference("FocusSessions")
                .orderByChild("userId").equalTo(user.getUid());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                todaySessions.clear();
                Calendar now = Calendar.getInstance();
                int today = now.get(Calendar.DAY_OF_YEAR);
                int year = now.get(Calendar.YEAR);

                List<FocusSession> all = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    FocusSession session = child.getValue(FocusSession.class);
                    if (session == null) continue;
                    all.add(session);

                    Calendar created = Calendar.getInstance();
                    created.setTimeInMillis(session.getCreatedAt());
                    if (created.get(Calendar.DAY_OF_YEAR) == today
                            && created.get(Calendar.YEAR) == year) {
                        todaySessions.add(session);
                    }
                }

                int totalMinutes = 0;
                for (FocusSession session : todaySessions) {
                    totalMinutes += session.getMinutes();
                }
                if (tvTodayTotal != null) {
                    tvTodayTotal.setText("Hôm nay bạn đã tập trung " + totalMinutes + " phút");
                }

                all.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                renderRecentSessions(all);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void renderRecentSessions(List<FocusSession> all) {
        if (!isAdded() || containerSessions == null) return;
        containerSessions.removeAllViews();

        if (all.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("Chưa có phiên nào. Bắt đầu một phiên 25 phút nhé!");
            empty.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextSecondary));
            empty.setTextSize(13f);
            containerSessions.addView(empty);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        int limit = Math.min(5, all.size());
        SimpleDateFormat format = new SimpleDateFormat("dd/MM • HH:mm", Locale.getDefault());

        for (int i = 0; i < limit; i++) {
            FocusSession session = all.get(i);
            TextView row = new TextView(requireContext());
            String title = !TextUtils.isEmpty(session.getTaskTitle())
                    ? session.getTaskTitle() : "Tập trung";
            row.setText("⏱ " + session.getMinutes() + " phút • " + title + "\n"
                    + format.format(new Date(session.getCreatedAt())));
            row.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextPrimary));
            row.setTextSize(13f);
            row.setBackgroundResource(R.drawable.bg_pill_neutral);
            row.setPadding(dp(14), dp(12), dp(14), dp(12));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = dp(8);
            row.setLayoutParams(params);
            containerSessions.addView(row);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopTimer();
    }
}
