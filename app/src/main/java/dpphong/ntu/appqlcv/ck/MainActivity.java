package dpphong.ntu.appqlcv.ck; // Giữ nguyên package chuẩn của bạn

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        float scale = Prefs.getFontScale(newBase);
        Configuration configuration = new Configuration(newBase.getResources().getConfiguration());
        configuration.fontScale = scale;
        super.attachBaseContext(newBase.createConfigurationContext(configuration));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        setupSystemBars();

        // 1. Ánh xạ Bottom Nav theo đúng ID trong file XML
        BottomNavigationView botnav = findViewById(R.id.bottomNavigationView);

        // 2. MẶC ĐỊNH KHI VỪA MỞ APP: Nạp sẵn màn hình Lịch trình
        if (savedInstanceState == null) {
            switchFragment(new CalendarFragment(), false);
        }

        // 3. Logic bắt sự kiện click tráo đổi Fragment
        botnav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment select_frag;
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                int itemid = menuItem.getItemId();

                // Khớp đúng ID: calendar, todo, diary, account từ file menu
                if (itemid == R.id.calendar) {
                    select_frag = new CalendarFragment();
                } else if (itemid == R.id.todo) {
                    select_frag = new TodoFragment();
                } else if (itemid == R.id.diary) {
                    select_frag = new DiaryFragment();
                } else if (itemid == R.id.account) {
                    if (currentUser != null) {
                        select_frag = new ProfileFragment();
                    } else {
                        select_frag = new LoginFragment();
                    }
                } else {
                    return false;
                }

                switchFragment(select_frag, true);
                return true;
            }
        });
    }

    /**
     * Xử lý tràn viền: đẩy nội dung xuống dưới thanh trạng thái và
     * chừa khoảng trống cho thanh điều hướng hệ thống.
     */
    private void setupSystemBars() {
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        int nightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isNight = nightMode == Configuration.UI_MODE_NIGHT_YES;
        controller.setAppearanceLightStatusBars(!isNight);
        controller.setAppearanceLightNavigationBars(!isNight);

        View container = findViewById(R.id.calendar_container);
        View bottomNav = findViewById(R.id.bottomNavigationView);

        ViewCompat.setOnApplyWindowInsetsListener(container, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, bars.top, 0, 0);
            return insets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, bars.bottom);
            return insets;
        });
    }

    /**
     * Chuyển màn hình với hiệu ứng mờ nhẹ, đồng thời dọn các màn hình
     * nhập liệu đang mở dở khi người dùng đổi tab.
     */
    private void switchFragment(Fragment fragment, boolean clearBackStack) {
        FragmentManager fm = getSupportFragmentManager();
        if (clearBackStack) {
            fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        fm.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.calendar_container, fragment)
                .commit();
    }
}
