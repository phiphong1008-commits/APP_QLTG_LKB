package dpphong.ntu.appqlcv.ck;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private TextView tvProfileName, tvProfileEmail;
    private TextView tvStatTotal, tvStatCompleted, tvStatPending;
    private Button btnLogout;
    private FirebaseAuth mAuth;
    private Button btnGoToTodo, btnGoToDiary;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Ánh xạ View
        mAuth = FirebaseAuth.getInstance();
        tvProfileName = view.findViewById(R.id.tv_profile_name);
        tvProfileEmail = view.findViewById(R.id.tv_profile_email);
        tvStatTotal = view.findViewById(R.id.tv_stat_total);
        tvStatCompleted = view.findViewById(R.id.tv_stat_completed);
        tvStatPending = view.findViewById(R.id.tv_stat_pending);
        btnLogout = view.findViewById(R.id.btn_logout);

        // 1. Hiển thị thông tin User
        loadUserInfo();

        // 2. Tải số liệu thống kê công việc từ Firebase
        loadTaskStatistics();
        btnGoToTodo = view.findViewById(R.id.btn_go_to_todo);
        btnGoToDiary = view.findViewById(R.id.btn_go_to_diary);

// Xử lý khi bấm "Quản lý công việc" -> Chuyển sang tab Todo
        btnGoToTodo.setOnClickListener(v -> {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottomNavigationView);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.todo); // Tự động sáng icon Todo và chuyển màn hình
            }
        });

// Xử lý khi bấm "Xem lại nhật ký" -> Chuyển sang tab Diary
        btnGoToDiary.setOnClickListener(v -> {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottomNavigationView);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.diary); // Tự động sáng icon Diary và chuyển màn hình
            }
        });

        // 3. Xử lý Đăng xuất
        // Bắt sự kiện click nút Đăng xuất trong ProfileFragment.java
        btnLogout.setOnClickListener(v -> {
            // 1. Xóa phiên đăng nhập trên Firebase
            mAuth.signOut();

            Toast.makeText(getContext(), "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();

            // 2. Lệnh chuyển ngay lập tức về trang LoginFragment
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.calendar_container, new LoginFragment())
                    .commit();
        });

        return view;
    }

    private void loadUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            tvProfileEmail.setText(currentUser.getEmail());

            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                tvProfileName.setText(displayName);
            } else if (currentUser.getEmail() != null) {
                // Cắt phần tên trước ký tự @ trong email nếu không có DisplayName
                tvProfileName.setText(currentUser.getEmail().split("@")[0]);
            }
        }
    }

    private void loadTaskStatistics() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String currentUserId = currentUser.getUid();

        // Truy vấn Firebase vào bảng Tasks, lọc theo userId
        Query query = FirebaseDatabase.getInstance().getReference("Tasks")
                .orderByChild("userId").equalTo(currentUserId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) { // Biến gốc là "snapshot"
                int total = 0;
                int taskCompletedCount = 0;
                int taskUncompletedCount = 0;

                // Đã sửa: Dùng taskSnapshot để duyệt qua snapshot.getChildren()
                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    Task task = taskSnapshot.getValue(Task.class);
                    if (task != null) {
                        total++; // Tăng tổng số công việc lên 1

                        if (task.isCompleted()) {
                            taskCompletedCount++; // Đưa vào nhóm Đã hoàn thành
                        } else {
                            taskUncompletedCount++; // Đưa vào nhóm Chưa hoàn thành
                        }
                    }
                }

                // Cập nhật số liệu chuẩn xác lên giao diện
                tvStatTotal.setText(String.valueOf(total));
                tvStatCompleted.setText(String.valueOf(taskCompletedCount));
                tvStatPending.setText(String.valueOf(taskUncompletedCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi tải thống kê: " + error.getMessage());
            }
        });
    }
}