package dpphong.ntu.appqlcv.ck;

import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SettingsFragment extends Fragment {

    private ChipGroup chipTheme, chipFont;
    private ActivityResultLauncher<String> jsonLauncher, csvLauncher, markdownLauncher;
    private TextView tvDiaryLockStatus;
    private MaterialButton btnDiaryLock, btnDiaryRemovePin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        ImageButton btnBack = view.findViewById(R.id.btn_back_settings);
        chipTheme = view.findViewById(R.id.chip_group_theme);
        chipFont = view.findViewById(R.id.chip_group_font);
        MaterialButton btnJson = view.findViewById(R.id.btn_export_json);
        MaterialButton btnCsv = view.findViewById(R.id.btn_export_csv);
        MaterialButton btnMarkdown = view.findViewById(R.id.btn_export_markdown);
        tvDiaryLockStatus = view.findViewById(R.id.tv_diary_lock_status);
        btnDiaryLock = view.findViewById(R.id.btn_diary_lock);
        btnDiaryRemovePin = view.findViewById(R.id.btn_diary_remove_pin);

        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        setupExportLaunchers();
        bindCurrentPreferences();

        chipTheme.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int id = group.getCheckedChipId();
            int mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            if (id == R.id.chip_theme_light) mode = AppCompatDelegate.MODE_NIGHT_NO;
            else if (id == R.id.chip_theme_dark) mode = AppCompatDelegate.MODE_NIGHT_YES;
            Prefs.setNightMode(requireContext(), mode);
        });

        chipFont.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int id = group.getCheckedChipId();
            float scale = 1f;
            if (id == R.id.chip_font_small) scale = 0.9f;
            else if (id == R.id.chip_font_large) scale = 1.15f;
            Prefs.setFontScale(requireContext(), scale);
            requireActivity().recreate();
        });

        btnJson.setOnClickListener(v -> jsonLauncher.launch(buildFileName("json")));
        btnCsv.setOnClickListener(v -> csvLauncher.launch(buildFileName("csv")));
        btnMarkdown.setOnClickListener(v -> markdownLauncher.launch(buildFileName("md")));

        btnDiaryLock.setOnClickListener(v -> showSetPinDialog());
        btnDiaryRemovePin.setOnClickListener(v -> showRemovePinDialog());
        updateDiaryLockUi();

        return view;
    }

    // ==========================================
    // KHÓA NHẬT KÝ BẰNG PIN
    // ==========================================
    private void updateDiaryLockUi() {
        if (!isAdded() || tvDiaryLockStatus == null) return;

        boolean hasPin = Prefs.hasDiaryPin(requireContext());
        if (hasPin) {
            tvDiaryLockStatus.setText("🔒 Đang bật khóa • Nhật ký yêu cầu mã PIN khi mở");
            tvDiaryLockStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorSuccess));
            btnDiaryLock.setText("Đổi mã PIN");
            btnDiaryRemovePin.setVisibility(View.VISIBLE);
        } else {
            tvDiaryLockStatus.setText("🔓 Chưa bật khóa nhật ký");
            tvDiaryLockStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextSecondary));
            btnDiaryLock.setText("Bật khóa nhật ký");
            btnDiaryRemovePin.setVisibility(View.GONE);
        }
    }

    private void showSetPinDialog() {
        boolean hasPin = Prefs.hasDiaryPin(requireContext());
        int padding = dp(20);

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(padding, dp(4), padding, 0);

        final EditText currentInput = hasPin ? makePinInput("Mã PIN hiện tại") : null;
        final EditText newInput = makePinInput("Mã PIN mới (4-6 chữ số)");
        final EditText confirmInput = makePinInput("Nhập lại mã PIN mới");

        if (currentInput != null) container.addView(currentInput);
        container.addView(newInput);
        container.addView(confirmInput);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(hasPin ? "Đổi mã PIN nhật ký" : "Bật khóa nhật ký")
                .setIcon(R.drawable.ic_lock)
                .setView(container)
                .setPositiveButton("Lưu", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    if (currentInput != null) {
                        String current = currentInput.getText().toString().trim();
                        if (!Prefs.checkDiaryPin(requireContext(), current)) {
                            currentInput.setError("Mã PIN hiện tại không đúng");
                            return;
                        }
                    }

                    String pin = newInput.getText().toString().trim();
                    String confirm = confirmInput.getText().toString().trim();
                    if (!pin.matches("\\d{4,6}")) {
                        newInput.setError("Mã PIN gồm 4-6 chữ số");
                        return;
                    }
                    if (!pin.equals(confirm)) {
                        confirmInput.setError("Mã nhập lại không khớp");
                        return;
                    }

                    Prefs.setDiaryPin(requireContext(), pin);
                    updateDiaryLockUi();
                    Toast.makeText(getContext(), "Đã bật khóa nhật ký",
                            Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }));
        dialog.show();
    }

    private void showRemovePinDialog() {
        final EditText input = makePinInput("Nhập mã PIN hiện tại");

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(20), dp(4), dp(20), 0);
        container.addView(input);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Tắt khóa nhật ký")
                .setIcon(R.drawable.ic_lock)
                .setView(container)
                .setPositiveButton("Tắt khóa", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {
                    String pin = input.getText().toString().trim();
                    if (!Prefs.checkDiaryPin(requireContext(), pin)) {
                        input.setError("Mã PIN không đúng");
                        return;
                    }
                    Prefs.clearDiaryPin(requireContext());
                    updateDiaryLockUi();
                    Toast.makeText(getContext(), "Đã tắt khóa nhật ký",
                            Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }));
        dialog.show();
    }

    private EditText makePinInput(String hint) {
        EditText input = new EditText(requireContext());
        input.setHint(hint);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(10);
        input.setLayoutParams(params);
        return input;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void setupExportLaunchers() {
        jsonLauncher = registerForActivityResult(new ActivityResultContracts.CreateDocument("application/json"),
                uri -> {
                    if (uri != null) exportData("json", uri);
                });
        csvLauncher = registerForActivityResult(new ActivityResultContracts.CreateDocument("text/csv"),
                uri -> {
                    if (uri != null) exportData("csv", uri);
                });
        markdownLauncher = registerForActivityResult(new ActivityResultContracts.CreateDocument("text/markdown"),
                uri -> {
                    if (uri != null) exportData("md", uri);
                });
    }

    private String buildFileName(String extension) {
        String date = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        return "cong-viec-" + date + "." + extension;
    }

    private void bindCurrentPreferences() {
        int nightMode = Prefs.getNightMode(requireContext());
        if (nightMode == AppCompatDelegate.MODE_NIGHT_NO) {
            chipTheme.check(R.id.chip_theme_light);
        } else if (nightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            chipTheme.check(R.id.chip_theme_dark);
        } else {
            chipTheme.check(R.id.chip_theme_system);
        }

        float scale = Prefs.getFontScale(requireContext());
        if (scale < 0.95f) chipFont.check(R.id.chip_font_small);
        else if (scale > 1.05f) chipFont.check(R.id.chip_font_large);
        else chipFont.check(R.id.chip_font_normal);
    }

    // ==========================================
    // XUẤT DỮ LIỆU
    // ==========================================
    private void exportData(String format, Uri uri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        Query tasksQuery = FirebaseDatabase.getInstance().getReference("Tasks")
                .orderByChild("userId").equalTo(user.getUid());

        tasksQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot tasksSnapshot) {
                Query diariesQuery = FirebaseDatabase.getInstance().getReference("Diaries")
                        .orderByChild("userId").equalTo(user.getUid());

                diariesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot diariesSnapshot) {
                        try {
                            String content;
                            switch (format) {
                                case "csv":
                                    content = buildCsv(tasksSnapshot);
                                    break;
                                case "md":
                                    content = buildMarkdown(tasksSnapshot, diariesSnapshot);
                                    break;
                                default:
                                    content = buildJson(tasksSnapshot, diariesSnapshot);
                                    break;
                            }
                            writeFile(uri, content);
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Lỗi tạo tệp: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Không tải được nhật ký", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Không tải được công việc", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private String buildJson(DataSnapshot tasks, DataSnapshot diaries) throws Exception {
        JSONObject root = new JSONObject();
        root.put("exportedAt", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date()));

        JSONArray taskArray = new JSONArray();
        for (DataSnapshot child : tasks.getChildren()) {
            Object value = child.getValue();
            if (value instanceof Map) {
                JSONObject item = new JSONObject((Map<String, Object>) value);
                item.put("id", child.getKey());
                taskArray.put(item);
            }
        }
        root.put("tasks", taskArray);

        JSONArray diaryArray = new JSONArray();
        for (DataSnapshot child : diaries.getChildren()) {
            Object value = child.getValue();
            if (value instanceof Map) {
                JSONObject item = new JSONObject((Map<String, Object>) value);
                item.put("id", child.getKey());
                diaryArray.put(item);
            }
        }
        root.put("diaries", diaryArray);

        return root.toString(2);
    }

    private String buildCsv(DataSnapshot tasks) {
        StringBuilder builder = new StringBuilder();
        builder.append("Tên,Mô tả,Ngày,Giờ,Ưu tiên,Trạng thái,Danh sách,Lặp lại\n");

        for (DataSnapshot child : tasks.getChildren()) {
            Task task = child.getValue(Task.class);
            if (task == null) continue;

            builder.append(csv(task.getTitle())).append(',')
                    .append(csv(task.getDescription())).append(',')
                    .append(csv(task.getDate())).append(',')
                    .append(csv(task.getTime())).append(',')
                    .append(csv(task.getPriority())).append(',')
                    .append(task.isCompleted() ? "Đã xong" : "Chưa xong").append(',')
                    .append(csv(task.getListName())).append(',')
                    .append(csv(task.getRecurrence())).append('\n');
        }
        return builder.toString();
    }

    private String buildMarkdown(DataSnapshot tasks, DataSnapshot diaries) {
        StringBuilder builder = new StringBuilder();
        builder.append("# Sao lưu công việc & nhật ký\n\n");
        builder.append("Xuất lúc: ")
                .append(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()))
                .append("\n\n## Công việc\n\n");

        List<Task> taskList = new ArrayList<>();
        for (DataSnapshot child : tasks.getChildren()) {
            Task task = child.getValue(Task.class);
            if (task != null) taskList.add(task);
        }

        if (taskList.isEmpty()) {
            builder.append("_Chưa có công việc nào._\n\n");
        } else {
            for (Task task : taskList) {
                builder.append("- [").append(task.isCompleted() ? "x" : " ").append("] **")
                        .append(task.getTitle() != null ? task.getTitle() : "Công việc").append("**");

                List<String> meta = new ArrayList<>();
                if (!TextUtils.isEmpty(task.getDate())) meta.add(task.getDate());
                if (!TextUtils.isEmpty(task.getTime())) meta.add(task.getTime());
                if (!TextUtils.isEmpty(task.getPriority())) meta.add("Ưu tiên " + task.getPriority());
                if (!TextUtils.isEmpty(task.getListName())) meta.add("Danh sách " + task.getListName());
                if (meta.size() > 0) {
                    builder.append(" — ").append(TextUtils.join(" • ", meta));
                }
                builder.append('\n');

                if (!TextUtils.isEmpty(task.getDescription())) {
                    builder.append("  > ").append(task.getDescription()).append('\n');
                }
            }
            builder.append('\n');
        }

        builder.append("## Nhật ký\n\n");
        int diaryCount = 0;
        for (DataSnapshot child : diaries.getChildren()) {
            Diary diary = child.getValue(Diary.class);
            if (diary == null) continue;
            diaryCount++;
            builder.append("### ").append(diary.getTitle() != null ? diary.getTitle() : "Nhật ký")
                    .append(" (").append(diary.getDate() != null ? diary.getDate() : "").append(")\n\n");
            if (diary.getContent() != null) {
                builder.append(diary.getContent()).append("\n\n");
            }
        }
        if (diaryCount == 0) {
            builder.append("_Chưa có nhật ký nào._\n");
        }
        return builder.toString();
    }

    private String csv(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private void writeFile(Uri uri, String content) {
        try (OutputStreamWriter writer = new OutputStreamWriter(
                requireContext().getContentResolver().openOutputStream(uri, "w"))) {
            writer.write(content);
            Toast.makeText(getContext(), "Đã xuất dữ liệu thành công", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Không ghi được tệp: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}
