package dpphong.ntu.appqlcv.ck; // Giữ nguyên package chuẩn của bạn

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        myRef.setValue("Hello, World!");
        // 1. Ánh xạ Bottom Nav theo đúng ID trong file XML của bạn
        BottomNavigationView botnav = findViewById(R.id.bottomNavigationView);

        // 2. MẶC ĐỊNH KHI VỪA MỞ APP: Nạp sẵn màn hình Lịch trình vào container
        // để người dùng thấy giao diện ngay mà không bị trống màn hình
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.calendar_container, new CalendarFragment())
                    .commit();
        }

        // 3. Logic bắt sự kiện click tráo đổi Fragment theo đúng ID bạn cung cấp
        botnav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment select_frag = null;
                int itemid = menuItem.getItemId();

                // Khớp đúng ID: calendar, account, diary từ file menu của bạn
                if (itemid == R.id.calendar) {
                    select_frag = new CalendarFragment(); // Màn hình bộ lịch ô vuông lồng icon
                }
                else if (itemid == R.id.todo) {
                    // select_frag = new AccountFragment(); // Màn hình Tài khoản (bạn tạo sau)
                }

                else if (itemid == R.id.diary) {
                    // select_frag = new DiaryFragment(); // Màn hình Nhật ký/Công việc (bạn tạo sau)
                }
                else if (itemid == R.id.account) {
                     select_frag = new LoginFragment(); // Màn hình Tài khoản (bạn tạo sau)
                }

                // 4. Nạp Fragment được chọn vào đúng hộp chứa calendar_container trong activity_main.xml
                if (select_frag != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.calendar_container, select_frag)
                            .commit();
                }
                return true;
            }
        });
    }
}