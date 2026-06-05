package dpphong.ntu.appqlcv.ck; // Thay bằng package của bạn

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "TASK_ALARM_CHANNEL";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 1. Nhận "gói hàng" (Tiêu đề và mô tả) từ nơi gửi
        String title = intent.getStringExtra("TASK_TITLE");
        String desc = intent.getStringExtra("TASK_DESC");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // 2. Tạo Kênh thông báo (Bắt buộc với Android 8.0 trở lên)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Thông báo công việc",
                    NotificationManager.IMPORTANCE_HIGH // Mức độ cao để nó réo lên
            );
            channel.setDescription("Kênh nhắc nhở công việc tới giờ");
            notificationManager.createNotificationChannel(channel);
        }

        // 3. (Tùy chọn) Khi bấm vào thông báo thì mở MainActivity
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 4. Thiết kế hình dáng của cái Thông báo
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_event) // BẠN NHỚ THAY ICON NÀY bằng icon có sẵn trong app của bạn
                .setContentTitle("Đến giờ rồi: " + title)
                .setContentText(desc)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true) // Bấm vào thì tự tắt
                .setContentIntent(pendingIntent);

        // 5. Bắn thông báo lên màn hình (Dùng thời gian hiện tại làm ID để các thông báo không đè lên nhau)
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
    }
}