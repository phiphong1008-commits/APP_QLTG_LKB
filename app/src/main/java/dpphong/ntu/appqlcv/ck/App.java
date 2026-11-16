package dpphong.ntu.appqlcv.ck;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Bật lưu dữ liệu ngoại tuyến cho Firebase và áp dụng chế độ tối đã chọn.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        AppCompatDelegate.setDefaultNightMode(Prefs.getNightMode(this));

        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception ignored) {
            // Persistence chỉ được bật một lần, bỏ qua nếu đã được khởi tạo
        }
    }
}
