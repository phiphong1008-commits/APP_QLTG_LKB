package dpphong.ntu.appqlcv.ck; // Thay bằng package name thực tế của bạn

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

public class RegisterFragment extends Fragment {

    private EditText edtEmail, edtPassword, edtConfirmPassword;
    private Button btnRegister;
    private TextView tvGoToLogin;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // Ánh xạ View
        edtEmail = view.findViewById(R.id.edtRegEmail);
        edtPassword = view.findViewById(R.id.edtRegPassword);
        edtConfirmPassword = view.findViewById(R.id.edtRegConfirmPassword);
        btnRegister = view.findViewById(R.id.btnRegister);
        tvGoToLogin = view.findViewById(R.id.tvGoToLogin);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Xử lý nút Đăng ký
        btnRegister.setOnClickListener(v -> registerUser());

        // Xử lý chuyển về màn hình Đăng nhập
        tvGoToLogin.setOnClickListener(v -> {
            // Viết code quay lại LoginFragment của bạn ở đây
        });

        return view;
    }

    private void registerUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        // Validate dữ liệu
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(getContext(), "Vui lòng nhập đủ các trường!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(getContext(), "Mật khẩu phải từ 6 ký tự trở lên!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Mật khẩu nhập lại không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi Firebase để tạo tài khoản
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                        // Chuyển hướng người dùng quay lại Login hoặc vào thẳng Calendar
                    } else {
                        Toast.makeText(getContext(), "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}