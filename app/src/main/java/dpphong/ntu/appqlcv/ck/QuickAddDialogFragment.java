package dpphong.ntu.appqlcv.ck;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Hộp thoại thêm công việc nhanh bằng câu tự nhiên, hỗ trợ nhập bằng giọng nói.
 */
public class QuickAddDialogFragment extends DialogFragment {

    private EditText edtInput;
    private TextView tvPreview;
    private ActivityResultLauncher<Intent> voiceLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        voiceLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        List<String> results = result.getData()
                                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        if (results != null && !results.isEmpty() && edtInput != null) {
                            edtInput.setText(results.get(0));
                            edtInput.setSelection(edtInput.getText().length());
                        }
                    }
                });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_quick_add, null, false);

        edtInput = view.findViewById(R.id.edt_quick_add);
        tvPreview = view.findViewById(R.id.tv_quick_preview);
        MaterialButton btnVoice = view.findViewById(R.id.btn_quick_voice);

        edtInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePreview(s != null ? s.toString() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        updatePreview("");

        btnVoice.setOnClickListener(v -> startVoiceInput());

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Thêm nhanh")
                .setIcon(R.drawable.ic_sparkle)
                .setView(view)
                .setPositiveButton("Thêm việc", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> saveQuickTask()));

        return dialog;
    }

    private void updatePreview(String text) {
        if (tvPreview == null) return;
        QuickAddParser.Result result = QuickAddParser.parse(text);
        String preview = result.summary + "\n📝 " + result.title;
        tvPreview.setText(preview);
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Nói công việc bạn muốn thêm");

        try {
            if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                voiceLauncher.launch(intent);
            } else {
                Toast.makeText(getContext(), "Thiết bị chưa có ứng dụng nhận giọng nói",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "Không mở được micro", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveQuickTask() {
        if (edtInput == null) return;
        String raw = edtInput.getText().toString().trim();
        if (TextUtils.isEmpty(raw)) {
            edtInput.setError("Nhập nội dung công việc");
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        QuickAddParser.Result result = QuickAddParser.parse(raw);
        if (TextUtils.isEmpty(result.title)) result.title = raw;

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        if (result.listName == null) {
            writeTask(database, user.getUid(), result, null);
            return;
        }

        Query query = database.getReference("Lists")
                .orderByChild("userId").equalTo(user.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                TaskList matched = null;
                for (DataSnapshot child : snapshot.getChildren()) {
                    TaskList taskList = child.getValue(TaskList.class);
                    if (taskList == null) continue;
                    if (taskList.getId() == null) taskList.setId(child.getKey());
                    if (taskList.getName() != null
                            && taskList.getName().equalsIgnoreCase(result.listName)) {
                        matched = taskList;
                        break;
                    }
                }
                writeTask(database, user.getUid(), result, matched);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                writeTask(database, user.getUid(), result, null);
            }
        });
    }

    private void writeTask(FirebaseDatabase database, String userId,
                           QuickAddParser.Result result, @Nullable TaskList list) {
        String taskId = database.getReference("Tasks").push().getKey();
        if (taskId == null) return;

        HashMap<String, Object> taskMap = new HashMap<>();
        taskMap.put("id", taskId);
        taskMap.put("userId", userId);
        taskMap.put("title", result.title);
        taskMap.put("description", "");
        taskMap.put("date", result.date);
        taskMap.put("time", result.time != null ? result.time : "");
        taskMap.put("priority", result.priority);
        taskMap.put("icon", "work");
        taskMap.put("isCompleted", false);
        taskMap.put("timestamp", System.currentTimeMillis());
        taskMap.put("listId", list != null ? list.getId() : null);
        taskMap.put("listName", list != null ? list.getName() : result.listName);
        taskMap.put("listColor", list != null ? list.getColor() : null);
        taskMap.put("recurrence", "none");
        taskMap.put("reminderOffsets", result.time != null
                ? Collections.singletonList(0L) : Collections.emptyList());
        taskMap.put("subtasks", Collections.emptyList());
        taskMap.put("nextSpawned", false);

        database.getReference("Tasks").child(taskId).setValue(taskMap)
                .addOnSuccessListener(aVoid -> {
                    if (result.time != null && getContext() != null) {
                        ReminderScheduler.schedule(requireContext(), taskId, result.title, "",
                                result.date, result.time, 0L);
                    }
                    Toast.makeText(getContext(), "Đã thêm: " + result.title,
                            Toast.LENGTH_SHORT).show();
                    dismissAllowingStateLoss();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Không lưu được: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
