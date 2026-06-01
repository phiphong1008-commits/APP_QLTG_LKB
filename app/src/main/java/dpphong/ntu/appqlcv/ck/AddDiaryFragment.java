package dpphong.ntu.appqlcv.ck;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

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

public class AddDiaryFragment extends Fragment {

    private Diary diaryToEdit = null; // Biến lưu trữ Nhật ký nếu đang ở chế độ sửa

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
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        btnSave.setOnClickListener(v -> saveDiaryToFirebase());

        // ==========================================
        // KIỂM TRA CHẾ ĐỘ (THÊM MỚI HAY CHỈNH SỬA)
        // ==========================================
        if (getArguments() != null && getArguments().containsKey("DIARY_EDIT")) {
            diaryToEdit = (Diary) getArguments().getSerializable("DIARY_EDIT");

            if (diaryToEdit != null) {
                // ĐANG Ở CHẾ ĐỘ CHỈNH SỬA
                tvHeaderTitle.setText("Sửa Nhật Ký");
                btnSave.setText("Cập nhật");

                // Đổ dữ liệu cũ lên giao diện
                edtTitle.setText(diaryToEdit.getTitle());
                edtContent.setText(diaryToEdit.getContent());

                // Giữ nguyên ngày cũ của nhật ký
                databaseDateString = diaryToEdit.getDate();
                tvDate.setText("Ngày: " + databaseDateString);
            }
        } else {
            // ĐANG Ở CHẾ ĐỘ THÊM MỚI
            tvHeaderTitle.setText("Viết Nhật Ký");
            btnSave.setText("Lưu nhật ký");
            setupCurrentDate(); // Chỉ thiết lập ngày hôm nay nếu là thêm mới
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
            showAlertDialog("Lỗi", "Vui lòng đăng nhập trước!");
            return;
        }

        String title = edtTitle.getText().toString().trim();
        String content = edtContent.getText().toString().trim();

        if (TextUtils.isEmpty(content)) {
            showAlertDialog("Cảnh báo", "Bạn chưa viết nội dung nhật ký!");
            return;
        }

        if (diaryToEdit != null) {
            // ==========================================
            // CHẾ ĐỘ CẬP NHẬT (UPDATE)
            // ==========================================
            HashMap<String, Object> updateMap = new HashMap<>();
            updateMap.put("title", title);
            updateMap.put("content", content);
            // Không cập nhật lại 'date' để giữ nguyên ngày tạo ban đầu

            mDatabase.child("Diaries").child(diaryToEdit.getId()).updateChildren(updateMap)
                    .addOnSuccessListener(aVoid -> {
                        // Lưu thành công thì quay về danh sách
                        getParentFragmentManager().popBackStack();
                    })
                    .addOnFailureListener(e -> showAlertDialog("Lỗi", e.getMessage()));
        } else {
            // ==========================================
            // CHẾ ĐỘ THÊM MỚI (INSERT)
            // ==========================================
            String diaryId = mDatabase.child("Diaries").push().getKey();

            HashMap<String, Object> diaryMap = new HashMap<>();
            diaryMap.put("id", diaryId);
            diaryMap.put("userId", currentUser.getUid());
            diaryMap.put("title", title);
            diaryMap.put("content", content);
            diaryMap.put("date", databaseDateString);
            diaryMap.put("timestamp", System.currentTimeMillis());

            if (diaryId != null) {
                mDatabase.child("Diaries").child(diaryId).setValue(diaryMap)
                        .addOnSuccessListener(aVoid -> {
                            // Thêm thành công thì quay về danh sách
                            getParentFragmentManager().popBackStack();
                        })
                        .addOnFailureListener(e -> showAlertDialog("Lỗi", e.getMessage()));
            }
        }
    }
}