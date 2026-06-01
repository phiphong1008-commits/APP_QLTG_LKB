package dpphong.ntu.appqlcv.ck;

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

public class LoginFragment extends Fragment {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView tvGoToRegister;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Ánh xạ View
        edtEmail = view.findViewById(R.id.edtLoginEmail);
        edtPassword = view.findViewById(R.id.edtLoginPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvGoToRegister = view.findViewById(R.id.tvGoToRegister);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Xử lý nút Đăng nhập
        btnLogin.setOnClickListener(v -> loginUser());

        // Xử lý chuyển sang màn hình Đăng ký
        tvGoToRegister.setOnClickListener(v -> {
            FragmentManager fm = getParentFragmentManager();
            fm.beginTransaction()
                    .replace(R.id.calendar_container, new RegisterFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void loginUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        boolean isValid = true;

        // Báo lỗi trực tiếp trên ô Email nếu để trống
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Vui lòng nhập Email!");
            edtEmail.requestFocus(); // Tự động đưa con trỏ chuột về ô này
            isValid = false;
        }

        // Báo lỗi trực tiếp trên ô Mật khẩu nếu để trống
        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Vui lòng nhập Mật khẩu!");
            if (isValid) edtPassword.requestFocus();
            isValid = false;
        }

        // Nếu có lỗi nhập liệu thì dừng lại, không gọi Firebase
        if (!isValid) return;

        // Gọi Firebase để đăng nhập
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Thành công: Chuyển thẳng sang trang Profile không cần thông báo
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.calendar_container, new ProfileFragment())
                                .commit();
                    } else {
                        // Thất bại: Báo lỗi sai tài khoản ngay trên ô mật khẩu
                        edtPassword.setError("Email hoặc mật khẩu không chính xác!");
                        edtPassword.requestFocus();
                    }
                });
    }
}