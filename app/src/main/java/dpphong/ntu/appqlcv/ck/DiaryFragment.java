package dpphong.ntu.appqlcv.ck; // Thay bằng package name của bạn

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class DiaryFragment extends Fragment {

    private TextView tvHeaderTitle, tvDate;
    private EditText edtTitle, edtContent;
    private Button btnSave;

    private String databaseDateString;

    // Khai báo Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diary, container, false);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Ánh xạ View
        tvHeaderTitle = view.findViewById(R.id.tv_header_title);
        tvDate = view.findViewById(R.id.tv_diary_date);
        edtTitle = view.findViewById(R.id.edt_diary_title);
        edtContent = view.findViewById(R.id.edt_diary_content);
        btnSave = view.findViewById(R.id.btn_save_diary);

        tvHeaderTitle.setText("Nhật Ký");

        setupCurrentDate();

        btnSave.setOnClickListener(v -> saveDiaryToFirebase());

        return view;
    }

    private void setupCurrentDate() {
        Date currentDate = new Date();
        SimpleDateFormat displayFormat = new SimpleDateFormat("'Hôm nay, 'dd/MM/yyyy", new Locale("vi", "VN"));
        tvDate.setText(displayFormat.format(currentDate));

        // Format chuẩn để lưu database: yyyy-MM-dd
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        databaseDateString = dbFormat.format(currentDate);
    }

    private void saveDiaryToFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập trước!", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = edtTitle.getText().toString().trim();
        String content = edtContent.getText().toString().trim();

        if (TextUtils.isEmpty(content)) {
            Toast.makeText(getContext(), "Bạn chưa viết nội dung nhật ký!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Tạo ID ngẫu nhiên cho bài nhật ký
        String diaryId = mDatabase.child("Diaries").push().getKey();

        // 2. Gom dữ liệu
        HashMap<String, Object> diaryMap = new HashMap<>();
        diaryMap.put("id", diaryId);
        diaryMap.put("userId", currentUser.getUid());
        diaryMap.put("title", title);
        diaryMap.put("content", content);
        diaryMap.put("date", databaseDateString);
        diaryMap.put("timestamp", System.currentTimeMillis());

        // 3. Đẩy lên Firebase
        if (diaryId != null) {
            mDatabase.child("Diaries").child(diaryId).setValue(diaryMap)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Đã lưu nhật ký!", Toast.LENGTH_SHORT).show();
                            // Reset lại nội dung để viết bài mới nếu muốn
                            edtTitle.setText("");
                            edtContent.setText("");
                        } else {
                            Toast.makeText(getContext(), "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}