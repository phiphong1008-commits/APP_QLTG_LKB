package dpphong.ntu.appqlcv.ck;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Phân loại công việc theo ma trận Eisenhower dựa trên ưu tiên và hạn chót.
 */
public class MatrixFragment extends Fragment {

    private LinearLayout q1, q2, q3, q4;
    private final List<Task> tasks = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_matrix, container, false);

        q1 = view.findViewById(R.id.container_matrix_q1);
        q2 = view.findViewById(R.id.container_matrix_q2);
        q3 = view.findViewById(R.id.container_matrix_q3);
        q4 = view.findViewById(R.id.container_matrix_q4);

        ImageButton btnBack = view.findViewById(R.id.btn_back_matrix);
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        loadTasks();
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
                    if (task != null && !task.isCompleted()) tasks.add(task);
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
        q1.removeAllViews();
        q2.removeAllViews();
        q3.removeAllViews();
        q4.removeAllViews();

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Calendar.getInstance().getTime());

        for (Task task : tasks) {
            boolean important = "Cao".equals(task.getPriority());
            boolean urgent = task.getDate() != null && task.getDate().compareTo(today) <= 0;

            LinearLayout container;
            if (important && urgent) container = q1;
            else if (important) container = q2;
            else if (urgent) container = q3;
            else container = q4;

            container.addView(buildTaskRow(task));
        }

        addEmptyIfNeeded(q1);
        addEmptyIfNeeded(q2);
        addEmptyIfNeeded(q3);
        addEmptyIfNeeded(q4);
    }

    private TextView buildTaskRow(Task task) {
        TextView row = new TextView(requireContext());
        String date = task.getDate() != null ? task.getDate() : "chưa có ngày";
        row.setText("• " + task.getTitle() + "\n  " + date + " • Ưu tiên "
                + (task.getPriority() != null ? task.getPriority() : "Vừa"));
        row.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextPrimary));
        row.setTextSize(13f);
        row.setPadding(dp(6), dp(8), dp(6), dp(8));
        row.setBackgroundResource(R.drawable.bg_pill_neutral);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(6);
        row.setLayoutParams(params);

        row.setOnClickListener(v -> openEditor(task));
        row.setOnLongClickListener(v -> {
            showPriorityDialog(task);
            return true;
        });
        return row;
    }

    private void addEmptyIfNeeded(LinearLayout container) {
        if (container.getChildCount() > 0) return;
        TextView empty = new TextView(requireContext());
        empty.setText("Không có việc nào");
        empty.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextHint));
        empty.setTextSize(12f);
        container.addView(empty);
    }

    private void showPriorityDialog(Task task) {
        String[] options = {"Cao (quan trọng)", "Vừa", "Thấp"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(task.getTitle())
                .setItems(options, (dialog, which) -> {
                    String priority = which == 0 ? "Cao" : (which == 2 ? "Thấp" : "Vừa");
                    if (task.getId() == null) return;
                    task.setPriority(priority);
                    HashMap<String, Object> update = new HashMap<>();
                    update.put("priority", priority);
                    FirebaseDatabase.getInstance().getReference("Tasks")
                            .child(task.getId()).updateChildren(update)
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(),
                                    "Đã đổi ưu tiên", Toast.LENGTH_SHORT).show());
                })
                .show();
    }

    private void openEditor(Task task) {
        AddTodoFragment fragment = new AddTodoFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("TASK_EDIT", task);
        fragment.setArguments(bundle);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.calendar_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
