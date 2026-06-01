package dpphong.ntu.appqlcv.ck; // Thay bằng package name của bạn

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
import android.widget.Toast;

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

    private EditText edtTitle, edtDesc;
    private TextView tvDate, tvTime, tvHeaderTitle;
    private RadioGroup rgPriority;
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

        tvHeaderTitle.setText("Công Việc");

        tvDate.setOnClickListener(v -> showDatePicker());
        tvTime.setOnClickListener(v -> showTimePicker());
        btnSave.setOnClickListener(v -> saveTaskToFirebase());
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        // Xử lý sự kiện click
        btnBack.setOnClickListener(v -> {
            // Kiểm tra xem trong BackStack có Fragment nào trước đó không
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack(); // Bật Fragment hiện tại ra, quay về Fragment cũ
            } else {
                requireActivity().onBackPressed(); // Backup: hành vi giống nút back vật lý của điện thoại
            }
        });
        return view;
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    myCalendar.set(Calendar.YEAR, year);
                    myCalendar.set(Calendar.MONTH, month);
                    myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    // Lưu ý: Định dạng yyyy-MM-dd để sau này dễ so sánh với Lịch
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
            Toast.makeText(getContext(), "Vui lòng đăng nhập trước!", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = edtTitle.getText().toString().trim();
        String desc = edtDesc.getText().toString().trim();
        String date = tvDate.getText().toString();
        String time = tvTime.getText().toString();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(getContext(), "Vui lòng nhập tên công việc", Toast.LENGTH_SHORT).show();
            return;
        }
        if (date.equals("Chọn ngày")) {
            Toast.makeText(getContext(), "Vui lòng chọn ngày thực hiện", Toast.LENGTH_SHORT).show();
            return;
        }

        String priority = "Vừa";
        int checkedId = rgPriority.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_high) priority = "Cao";
        else if (checkedId == R.id.rb_low) priority = "Thấp";

        // 1. Tạo một ID ngẫu nhiên cho Task này
        String taskId = mDatabase.child("Tasks").push().getKey();

        // 2. Gom dữ liệu vào HashMap (Hoặc bạn có thể dùng Object TaskModel đã tạo trước đó)
        HashMap<String, Object> taskMap = new HashMap<>();
        taskMap.put("id", taskId);
        taskMap.put("userId", currentUser.getUid());
        taskMap.put("title", title);
        taskMap.put("description", desc);
        taskMap.put("date", date);
        taskMap.put("time", time.equals("Chọn giờ") ? "" : time);
        taskMap.put("priority", priority);
        taskMap.put("isCompleted", false);
        taskMap.put("timestamp", System.currentTimeMillis());

        // 3. Đẩy lên Firebase
        if (taskId != null) {
            mDatabase.child("Tasks").child(taskId).setValue(taskMap)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Đã lưu công việc thành công!", Toast.LENGTH_SHORT).show();
                            // Reset lại form nhập liệu
                            edtTitle.setText("");
                            edtDesc.setText("");
                            tvDate.setText("Chọn ngày");
                            tvTime.setText("Chọn giờ");
                            rgPriority.check(R.id.rb_medium);
                        } else {
                            Toast.makeText(getContext(), "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}