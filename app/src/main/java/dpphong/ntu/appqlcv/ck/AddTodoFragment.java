package dpphong.ntu.appqlcv.ck;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
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

public class AddTodoFragment extends Fragment {
    private Task taskToEdit = null;
    private EditText edtTitle, edtDesc, edtSubtask;
    private TextView tvDate, tvTime, tvHeaderTitle;
    private RadioGroup rgPriority;
    private RadioGroup rgTaskIcons;
    private Button btnSave;
    private ImageButton btnAddSubtask;
    private LinearLayout containerSubtasks;
    private MaterialAutoCompleteTextView actvTaskList;
    private ChipGroup chipGroupRecurrence, chipGroupReminders;

    private final Calendar myCalendar = Calendar.getInstance();

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private final List<TaskList> allLists = new ArrayList<>();
    private final List<String> listOptions = new ArrayList<>();
    private ArrayAdapter<String> listAdapter;
    private TaskList selectedList = null;

    private final List<Subtask> subtasks = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_todo, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        tvHeaderTitle = view.findViewById(R.id.tv_header_title);
        edtTitle = view.findViewById(R.id.edt_task_title);
        edtDesc = view.findViewById(R.id.edt_task_desc);
        tvDate = view.findViewById(R.id.tv_task_date);
        tvTime = view.findViewById(R.id.tv_task_time);
        rgPriority = view.findViewById(R.id.rg_priority);
        btnSave = view.findViewById(R.id.btn_save_task);
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        rgTaskIcons = view.findViewById(R.id.rg_task_icons);
        edtSubtask = view.findViewById(R.id.edt_subtask);
        btnAddSubtask = view.findViewById(R.id.btn_add_subtask);
        containerSubtasks = view.findViewById(R.id.container_subtasks);
        actvTaskList = view.findViewById(R.id.actv_task_list);
        chipGroupRecurrence = view.findViewById(R.id.chip_group_recurrence);
        chipGroupReminders = view.findViewById(R.id.chip_group_reminders);

        tvDate.setOnClickListener(v -> showDatePicker());
        tvTime.setOnClickListener(v -> showTimePicker());
        btnSave.setOnClickListener(v -> saveTaskToFirebase());
        btnAddSubtask.setOnClickListener(v -> addSubtaskFromInput());
        edtSubtask.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addSubtaskFromInput();
                return true;
            }
            return false;
        });

        setupListDropdown();

        if (getArguments() != null && getArguments().containsKey("TASK_EDIT")) {
            taskToEdit = (Task) getArguments().getSerializable("TASK_EDIT");

            if (taskToEdit != null) {
                tvHeaderTitle.setText("Sửa Công Việc");
                btnSave.setText("Cập nhật");

                edtTitle.setText(taskToEdit.getTitle());
                edtDesc.setText(taskToEdit.getDescription());
                tvDate.setText(taskToEdit.getDate());

                if (taskToEdit.getTime() != null && !taskToEdit.getTime().isEmpty()) {
                    tvTime.setText(taskToEdit.getTime());
                } else {
                    tvTime.setText("Chọn giờ");
                }

                if (taskToEdit.getPriority() != null) {
                    switch (taskToEdit.getPriority()) {
                        case "Cao":
                            rgPriority.check(R.id.rb_high);
                            break;
                        case "Thấp":
                            rgPriority.check(R.id.rb_low);
                            break;
                        default:
                            rgPriority.check(R.id.rb_medium);
                            break;
                    }
                }

                if (taskToEdit.getIcon() != null) {
                    switch (taskToEdit.getIcon()) {
                        case "study":
                            rgTaskIcons.check(R.id.rb_icon_study);
                            break;
                        case "home":
                            rgTaskIcons.check(R.id.rb_icon_home);
                            break;
                        case "event":
                            rgTaskIcons.check(R.id.rb_icon_event);
                            break;
                        default:
                            rgTaskIcons.check(R.id.rb_icon_work);
                            break;
                    }
                }

                // Việc con
                if (taskToEdit.getSubtasks() != null) {
                    for (Subtask subtask : taskToEdit.getSubtasks()) {
                        if (subtask != null) {
                            subtasks.add(new Subtask(subtask.getId(), subtask.getTitle(), subtask.isCompleted()));
                        }
                    }
                }

                // Lặp lại
                checkRecurrenceChip(taskToEdit.getRecurrence());

                // Các mốc nhắc
                renderReminderSelection(taskToEdit.getReminderOffsets());
            }
        } else {
            tvHeaderTitle.setText("Tạo Công Việc");
            btnSave.setText("Lưu công việc");
        }

        renderSubtasks();
        loadLists();

        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        if (taskToEdit == null && getArguments() != null) {
            String prefillDate = getArguments().getString("TASK_DATE");
            if (prefillDate != null && !prefillDate.isEmpty()) {
                tvDate.setText(prefillDate);
                try {
                    Date parsed = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(prefillDate);
                    if (parsed != null) {
                        myCalendar.setTime(parsed);
                    }
                } catch (Exception ignored) {
                }
            }
        }

        return view;
    }

    // ==========================================
    // DANH SÁCH
    // ==========================================
    private void setupListDropdown() {
        listOptions.clear();
        listOptions.add("— Không có danh sách —");
        listAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, listOptions);
        actvTaskList.setAdapter(listAdapter);
        actvTaskList.setText(listOptions.get(0), false);

        actvTaskList.setOnItemClickListener((parent, view, position, id) -> {
            if (position <= 0) {
                selectedList = null;
            } else {
                selectedList = allLists.get(position - 1);
            }
        });
    }

    private void loadLists() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        Query query = mDatabase.child("Lists")
                .orderByChild("userId")
                .equalTo(currentUser.getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || actvTaskList == null) return;

                allLists.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    TaskList taskList = child.getValue(TaskList.class);
                    if (taskList != null) {
                        if (taskList.getId() == null) taskList.setId(child.getKey());
                        allLists.add(taskList);
                    }
                }

                listOptions.clear();
                listOptions.add("— Không có danh sách —");
                for (TaskList taskList : allLists) {
                    listOptions.add(taskList.getName() != null ? taskList.getName() : "Danh sách");
                }
                listAdapter.notifyDataSetChanged();

                // Chọn lại danh sách đang sửa (nếu có)
                if (taskToEdit != null && taskToEdit.getListId() != null) {
                    for (int i = 0; i < allLists.size(); i++) {
                        if (taskToEdit.getListId().equals(allLists.get(i).getId())) {
                            selectedList = allLists.get(i);
                            actvTaskList.setText(selectedList.getName(), false);
                            break;
                        }
                    }
                } else {
                    actvTaskList.setText(listOptions.get(0), false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Không chặn việc lưu công việc nếu tải danh sách lỗi
            }
        });
    }

    // ==========================================
    // VIỆC CON
    // ==========================================
    private void addSubtaskFromInput() {
        String text = edtSubtask.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        subtasks.add(new Subtask("st_" + System.currentTimeMillis(), text, false));
        edtSubtask.setText("");
        renderSubtasks();
    }

    private void renderSubtasks() {
        containerSubtasks.removeAllViews();

        if (subtasks.isEmpty()) {
            containerSubtasks.setVisibility(View.GONE);
            return;
        }

        containerSubtasks.setVisibility(View.VISIBLE);
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (int i = 0; i < subtasks.size(); i++) {
            final int index = i;
            Subtask subtask = subtasks.get(i);

            View row = inflater.inflate(R.layout.item_subtask_edit, containerSubtasks, false);
            CheckBox checkBox = row.findViewById(R.id.cb_subtask_edit);
            TextView title = row.findViewById(R.id.tv_subtask_edit);
            ImageButton remove = row.findViewById(R.id.btn_remove_subtask);

            title.setText(subtask.getTitle());
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(subtask.isCompleted());
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> subtask.setCompleted(isChecked));

            remove.setOnClickListener(v -> {
                if (index < subtasks.size()) {
                    subtasks.remove(index);
                    renderSubtasks();
                }
            });

            containerSubtasks.addView(row);
        }
    }

    // ==========================================
    // LẶP LẠI & NHẮC NHỞ
    // ==========================================
    private void checkRecurrenceChip(String recurrence) {
        if (recurrence == null) recurrence = "none";
        switch (recurrence) {
            case "daily":
                chipGroupRecurrence.check(R.id.chip_recur_daily);
                break;
            case "weekly":
                chipGroupRecurrence.check(R.id.chip_recur_weekly);
                break;
            case "monthly":
                chipGroupRecurrence.check(R.id.chip_recur_monthly);
                break;
            default:
                chipGroupRecurrence.check(R.id.chip_recur_none);
                break;
        }
    }

    private String getSelectedRecurrence() {
        int checked = chipGroupRecurrence.getCheckedChipId();
        if (checked == R.id.chip_recur_daily) return "daily";
        if (checked == R.id.chip_recur_weekly) return "weekly";
        if (checked == R.id.chip_recur_monthly) return "monthly";
        return "none";
    }

    private void renderReminderSelection(List<Long> offsets) {
        chipGroupReminders.clearCheck();

        if (offsets == null || offsets.isEmpty()) {
            chipGroupReminders.check(R.id.chip_remind_on_time);
            return;
        }

        for (Long offset : offsets) {
            if (offset == null) continue;
            if (offset == 0) chipGroupReminders.check(R.id.chip_remind_on_time);
            else if (offset == 10) chipGroupReminders.check(R.id.chip_remind_10m);
            else if (offset == 60) chipGroupReminders.check(R.id.chip_remind_1h);
            else if (offset == 1440) chipGroupReminders.check(R.id.chip_remind_1d);
        }
    }

    private List<Long> getSelectedReminderOffsets() {
        List<Long> offsets = new ArrayList<>();
        if (isReminderChecked(R.id.chip_remind_on_time)) offsets.add(0L);
        if (isReminderChecked(R.id.chip_remind_10m)) offsets.add(10L);
        if (isReminderChecked(R.id.chip_remind_1h)) offsets.add(60L);
        if (isReminderChecked(R.id.chip_remind_1d)) offsets.add(1440L);
        return offsets;
    }

    private boolean isReminderChecked(int chipId) {
        View chip = chipGroupReminders.findViewById(chipId);
        return chip instanceof Chip && ((Chip) chip).isChecked();
    }

    // ==========================================
    // CHỌN NGÀY / GIỜ
    // ==========================================
    private void showAlertDialog(String title, String message) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("Đồng ý", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    myCalendar.set(Calendar.YEAR, year);
                    myCalendar.set(Calendar.MONTH, month);
                    myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    tvDate.setText(sdf.format(myCalendar.getTime()));
                },
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    myCalendar.set(Calendar.MINUTE, minute);
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    tvTime.setText(sdf.format(myCalendar.getTime()));
                },
                myCalendar.get(Calendar.HOUR_OF_DAY),
                myCalendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    // ==========================================
    // LƯU CÔNG VIỆC
    // ==========================================
    private void saveTaskToFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            showAlertDialog("Lỗi", "Vui lòng đăng nhập trước!");
            return;
        }

        String title = edtTitle.getText().toString().trim();
        String desc = edtDesc.getText().toString().trim();
        String date = tvDate.getText().toString();
        String time = tvTime.getText().toString();

        if (TextUtils.isEmpty(title)) {
            showAlertDialog("Cảnh báo", "Vui lòng nhập tên công việc!");
            return;
        }
        if (date.equals("Chọn ngày")) {
            showAlertDialog("Cảnh báo", "Vui lòng chọn ngày thực hiện!");
            return;
        }

        String priority = "Vừa";
        int checkedId = rgPriority.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_high) priority = "Cao";
        else if (checkedId == R.id.rb_low) priority = "Thấp";

        String iconType = "work";
        int checkedIconId = rgTaskIcons.getCheckedRadioButtonId();
        if (checkedIconId == R.id.rb_icon_study) iconType = "study";
        else if (checkedIconId == R.id.rb_icon_home) iconType = "home";
        else if (checkedIconId == R.id.rb_icon_event) iconType = "event";

        String timeValue = time.equals("Chọn giờ") ? "" : time;
        String recurrence = getSelectedRecurrence();
        List<Long> reminderOffsets = getSelectedReminderOffsets();

        String listId = selectedList != null ? selectedList.getId() : null;
        String listName = selectedList != null ? selectedList.getName() : null;
        String listColor = selectedList != null ? selectedList.getColor() : null;

        if (taskToEdit != null) {
            HashMap<String, Object> updateMap = new HashMap<>();
            updateMap.put("title", title);
            updateMap.put("description", desc);
            updateMap.put("date", date);
            updateMap.put("time", timeValue);
            updateMap.put("priority", priority);
            updateMap.put("icon", iconType);
            updateMap.put("listId", listId);
            updateMap.put("listName", listName);
            updateMap.put("listColor", listColor);
            updateMap.put("recurrence", recurrence);
            updateMap.put("reminderOffsets", reminderOffsets);
            updateMap.put("subtasks", subtasks);

            mDatabase.child("Tasks").child(taskToEdit.getId()).updateChildren(updateMap)
                    .addOnSuccessListener(aVoid -> {
                        scheduleReminders(taskToEdit.getId(), title, desc, date, timeValue, reminderOffsets);
                        getParentFragmentManager().popBackStack();
                    })
                    .addOnFailureListener(e -> showAlertDialog("Lỗi", e.getMessage()));

        } else {
            String taskId = mDatabase.child("Tasks").push().getKey();

            HashMap<String, Object> taskMap = new HashMap<>();
            taskMap.put("id", taskId);
            taskMap.put("userId", currentUser.getUid());
            taskMap.put("title", title);
            taskMap.put("description", desc);
            taskMap.put("date", date);
            taskMap.put("time", timeValue);
            taskMap.put("priority", priority);
            taskMap.put("icon", iconType);
            taskMap.put("isCompleted", false);
            taskMap.put("timestamp", System.currentTimeMillis());
            taskMap.put("listId", listId);
            taskMap.put("listName", listName);
            taskMap.put("listColor", listColor);
            taskMap.put("recurrence", recurrence);
            taskMap.put("reminderOffsets", reminderOffsets);
            taskMap.put("subtasks", subtasks);
            taskMap.put("nextSpawned", false);

            if (taskId != null) {
                mDatabase.child("Tasks").child(taskId).setValue(taskMap)
                        .addOnSuccessListener(aVoid -> {
                            scheduleReminders(taskId, title, desc, date, timeValue, reminderOffsets);
                            getParentFragmentManager().popBackStack();
                        })
                        .addOnFailureListener(e -> showAlertDialog("Lỗi", e.getMessage()));
            }
        }
    }

    private void scheduleReminders(String taskId, String title, String desc, String date,
                                   String time, List<Long> offsets) {
        ReminderScheduler.cancel(requireContext(), taskId);

        if (TextUtils.isEmpty(time) || offsets == null || offsets.isEmpty()) return;

        for (Long offset : offsets) {
            if (offset == null) continue;
            ReminderScheduler.schedule(requireContext(), taskId, title, desc, date, time, offset);
        }
    }
}
