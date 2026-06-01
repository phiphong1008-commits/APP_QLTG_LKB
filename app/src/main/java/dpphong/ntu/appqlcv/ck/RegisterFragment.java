package dpphong.ntu.appqlcv.ck; // Thay bằng package name thực tế của bạn

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

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
            FragmentManager fm = getParentFragmentManager();
            fm.beginTransaction()
                    .replace(R.id.calendar_container, new LoginFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void registerUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        boolean isValid = true;

        // 1. Kiểm tra Email
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lòng nhập Email!");
            edtEmail.requestFocus();
            isValid = false;
        }

        // 2. Kiểm tra Mật khẩu
        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Vui lòng nhập Mật khẩu!");
            if (isValid) edtPassword.requestFocus();
            isValid = false;
        } else if (password.length() < 6) {
            edtPassword.setError("Mật khẩu phải từ 6 ký tự trở lên!");
            if (isValid) edtPassword.requestFocus();
            isValid = false;
        }

        // 3. Kiểm tra Xác nhận mật khẩu
        if (TextUtils.isEmpty(confirmPassword)) {
            edtConfirmPassword.setError("Vui lòng xác nhận Mật khẩu!");
            if (isValid) edtConfirmPassword.requestFocus();
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            edtConfirmPassword.setError("Mật khẩu nhập lại không khớp!");
            if (isValid) edtConfirmPassword.requestFocus();
            isValid = false;
        }

        // Nếu có bất kỳ lỗi nhập liệu nào thì dừng lại
        if (!isValid) return;

        // Gọi Firebase để tạo tài khoản
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Đăng ký thành công, Firebase tự động đăng nhập.
                        // Chuyển thẳng sang trang Profile (hoặc Todo) mà không cần Toast
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.calendar_container, new ProfileFragment())
                                .commit();
                    } else {
                        // Lỗi từ phía máy chủ (Ví dụ: Email đã được sử dụng, sai định dạng email...)
                        edtEmail.setError("Đăng ký thất bại: " + task.getException().getMessage());
                        edtEmail.requestFocus();
                    }
                });
    }
}