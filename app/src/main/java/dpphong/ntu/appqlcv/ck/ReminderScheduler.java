package dpphong.ntu.appqlcv.ck;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Đặt nhiều lời nhắc cho một công việc dựa trên danh sách mốc thời gian
 * (tính bằng phút trước giờ thực hiện). 0 = đúng giờ.
 */
public final class ReminderScheduler {

    private ReminderScheduler() {
    }

    public static void schedule(Context context, Task task) {        if (context == null || task == null || TextUtils.isEmpty(task.getId())) return;

        List<Long> offsets = task.getReminderOffsets();
        if (offsets == null || offsets.isEmpty()) {
            offsets = Collections.singletonList(0L);
        }
        for (Long offset : offsets) {
            if (offset == null) continue;
            schedule(context, task.getId(), task.getTitle(), task.getDescription(),
                    task.getDate(), task.getTime(), offset);
        }
    }

    public static void schedule(Context context, String taskId, String title, String description,
                                String date, String time, long offsetMinutes) {
        if (context == null || TextUtils.isEmpty(taskId)
                || TextUtils.isEmpty(date) || TextUtils.isEmpty(time)) {
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date due = sdf.parse(date + " " + time);
            if (due == null) return;

            long triggerAt = due.getTime() - offsetMinutes * 60_000L;
            if (triggerAt <= System.currentTimeMillis()) return;

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.putExtra("TASK_TITLE", title);
            intent.putExtra("TASK_DESC", description);
            intent.putExtra("TASK_ID", taskId);
            intent.putExtra("TASK_OFFSET", offsetMinutes);

            int requestCode = (taskId + "#" + offsetMinutes).hashCode();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
                }
            } catch (SecurityException securityException) {
                // Android 12+ có thể chặn báo thức chính xác nếu người dùng chưa cấp quyền
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
            }

            Log.d("ALARM", "Đã đặt nhắc trước " + offsetMinutes + " phút: " + date + " " + time);
        } catch (Exception e) {
            Log.e("ALARM", "Lỗi đặt báo thức: " + e.getMessage());
        }
    }

    /**
     * Hủy toàn bộ các mốc nhắc đã đặt cho công việc trước khi đặt lại.
     */
    public static void cancel(Context context, String taskId) {
        if (context == null || TextUtils.isEmpty(taskId)) return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        for (long offset : new long[]{0L, 10L, 60L, 1440L}) {
            Intent intent = new Intent(context, NotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (taskId + "#" + offset).hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }
}
