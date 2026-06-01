package dpphong.ntu.appqlcv.ck;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class AddTodoFragment extends Fragment {
    private Task taskToEdit = null;
    private EditText edtTitle, edtDesc;
    private TextView tvDate, tvTime, tvHeaderTitle;
    private RadioGroup rgPriority;
    private RadioGroup rgTaskIcons; // Khai báo RadioGroup cho Icon
    private Button btnSave;

    private final Calendar myCalendar = Calendar.getInstance();

    // Khai báo Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_todo, container, false);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Ánh xạ View
        tvHeaderTitle = view.findViewById(R.id.tv_header_title);
        edtTitle = view.findViewById(R.id.edt_task_title);
        edtDesc = view.findViewById(R.id.edt_task_desc);
        tvDate = view.findViewById(R.id.tv_task_date);
        tvTime = view.findViewById(R.id.tv_task_time);
        rgPriority = view.findViewById(R.id.rg_priority);
        btnSave = view.findViewById(R.id.btn_save_task);
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        rgTaskIcons = view.findViewById(R.id.rg_task_icons); // Ánh xạ rg_task_icons

        tvDate.setOnClickListener(v -> showDatePicker());
        tvTime.setOnClickListener(v -> showTimePicker());
        btnSave.setOnClickListener(v -> saveTaskToFirebase());

        // ==========================================
        // KIỂM TRA CHẾ ĐỘ (THÊM MỚI HAY CHỈNH SỬA)
        // ==========================================
        if (getArguments() != null && getArguments().containsKey("TASK_EDIT")) {
            taskToEdit = (Task) getArguments().getSerializable("TASK_EDIT");

            if (taskToEdit != null) {
                // ĐANG Ở CHẾ ĐỘ CHỈNH SỬA
                tvHeaderTitle.setText("Sửa Công Việc");
                btnSave.setText("Cập nhật");

                // Đổ toàn bộ dữ liệu cũ lên giao diện
                edtTitle.setText(taskToEdit.getTitle());
                edtDesc.setText(taskToEdit.getDescription());
                tvDate.setText(taskToEdit.getDate());

                if (taskToEdit.getTime() != null && !taskToEdit.getTime().isEmpty()) {
                    tvTime.setText(taskToEdit.getTime());
                } else {
                    tvTime.setText("Chọn giờ");
                }

                // Đổ dữ liệu mức độ ưu tiên
                if (taskToEdit.getPriority() != null) {
                    switch (taskToEdit.getPriority()) {
                        case "Cao": rgPriority.check(R.id.rb_high); break;
                        case "Thấp": rgPriority.check(R.id.rb_low); break;
                        default: rgPriority.check(R.id.rb_medium); break;
                    }
                }

                // Đổ dữ liệu Icon cũ lên giao diện
                if (taskToEdit.getIcon() != null) {
                    switch (taskToEdit.getIcon()) {
                        case "study": rgTaskIcons.check(R.id.rb_icon_study); break;
                        case "home": rgTaskIcons.check(R.id.rb_icon_home); break;
                        case "event": rgTaskIcons.check(R.id.rb_icon_event); break;
                        default: rgTaskIcons.check(R.id.rb_icon_work); break;
                    }
                }
            }
        } else {
            // ĐANG Ở CHẾ ĐỘ THÊM MỚI
            tvHeaderTitle.setText("Tạo Công Việc");
            btnSave.setText("Lưu công việc");
        }

        // Xử lý sự kiện click nút Back
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        return view;
    }

    // Hàm tạo hộp thoại thông báo (Thay thế cho Toast)
    private void showAlertDialog(String title, String message) {
        new AlertDialog.Builder(requireContext())
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

        // Validate bằng AlertDialog
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

        // Lấy giá trị Icon người dùng đang chọn
        String iconType = "work"; // Mặc định là work
        int checkedIconId = rgTaskIcons.getCheckedRadioButtonId();
        if (checkedIconId == R.id.rb_icon_study) iconType = "study";
        else if (checkedIconId == R.id.rb_icon_home) iconType = "home";
        else if (checkedIconId == R.id.rb_icon_event) iconType = "event";

        if (taskToEdit != null) {
            // ==========================================
            // CHẾ ĐỘ CẬP NHẬT (UPDATE)
            // ==========================================
            HashMap<String, Object> updateMap = new HashMap<>();
            updateMap.put("title", title);
            updateMap.put("description", desc);
            updateMap.put("date", date);
            updateMap.put("time", time.equals("Chọn giờ") ? "" : time);
            updateMap.put("priority", priority);
            updateMap.put("icon", iconType); // Cập nhật lại Icon

            // Dùng updateChildren để chỉ cập nhật các trường bị đổi, không làm mất isCompleted
            mDatabase.child("Tasks").child(taskToEdit.getId()).updateChildren(updateMap)
                    .addOnSuccessListener(aVoid -> {
                        getParentFragmentManager().popBackStack();
                    })
                    .addOnFailureListener(e -> showAlertDialog("Lỗi", e.getMessage()));

        } else {
            // ==========================================
            // CHẾ ĐỘ THÊM MỚI (INSERT)
            // ==========================================
            String taskId = mDatabase.child("Tasks").push().getKey();

            HashMap<String, Object> taskMap = new HashMap<>();
            taskMap.put("id", taskId);
            taskMap.put("userId", currentUser.getUid());
            taskMap.put("title", title);
            taskMap.put("description", desc);
            taskMap.put("date", date);
            taskMap.put("time", time.equals("Chọn giờ") ? "" : time);
            taskMap.put("priority", priority);
            taskMap.put("icon", iconType); // Lưu Icon mới
            taskMap.put("isCompleted", false);
            taskMap.put("timestamp", System.currentTimeMillis());

            if (taskId != null) {
                mDatabase.child("Tasks").child(taskId).setValue(taskMap)
                        .addOnSuccessListener(aVoid -> {
                            getParentFragmentManager().popBackStack();
                        })
                        .addOnFailureListener(e -> showAlertDialog("Lỗi", e.getMessage()));
            }
        }
    }
}