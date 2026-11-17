package dpphong.ntu.appqlcv.ck;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Tiện ích nhỏ cho P1/P2: thêm việc vào lịch hệ thống (Google Calendar)
 * và chia sẻ việc qua ứng dụng khác.
 */
public final class CalendarHelper {

    private CalendarHelper() {
    }

    public static void addToCalendar(Context context, Task task) {
        if (context == null || task == null) return;

        long startMillis = parseStartMillis(task);
        long endMillis = startMillis + 60 * 60 * 1000L;

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                .putExtra(CalendarContract.Events.TITLE, task.getTitle())
                .putExtra(CalendarContract.Events.DESCRIPTION, task.getDescription())
                .putExtra(CalendarContract.Events.EVENT_LOCATION, task.getLocationName())
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "Không tìm thấy ứng dụng lịch trên máy", Toast.LENGTH_SHORT).show();
        }
    }

    public static void shareTask(Context context, Task task) {
        if (context == null || task == null) return;

        StringBuilder builder = new StringBuilder();
        builder.append("📌 ").append(task.getTitle() != null ? task.getTitle() : "Công việc");
        if (!TextUtils.isEmpty(task.getDate())) {
            builder.append("\n📅 ").append(task.getDate());
        }
        if (!TextUtils.isEmpty(task.getTime())) {
            builder.append(" ").append(task.getTime());
        }
        if (!TextUtils.isEmpty(task.getListName())) {
            builder.append("\n🗂️ ").append(task.getListName());
        }
        if (!TextUtils.isEmpty(task.getPriority())) {
            builder.append("\n⚡ Ưu tiên: ").append(task.getPriority());
        }
        if (!TextUtils.isEmpty(task.getDescription())) {
            builder.append("\n\n").append(task.getDescription());
        }
        builder.append("\n\n— chia sẻ từ APP_QLTG");

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, task.getTitle());
        intent.putExtra(Intent.EXTRA_TEXT, builder.toString());
        context.startActivity(Intent.createChooser(intent, "Chia sẻ công việc"));
    }

    private static long parseStartMillis(Task task) {
        Calendar calendar = Calendar.getInstance();
        String date = task.getDate();
        String time = task.getTime();

        try {
            if (!TextUtils.isEmpty(date)) {
                Date parsedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date);
                if (parsedDate != null) calendar.setTime(parsedDate);
            }
            if (!TextUtils.isEmpty(time)) {
                Date parsedTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).parse(time);
                if (parsedTime != null) {
                    Calendar timeCal = Calendar.getInstance();
                    timeCal.setTime(parsedTime);
                    calendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
                    calendar.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
                    calendar.set(Calendar.SECOND, 0);
                }
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, 9);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
            }
        } catch (Exception ignored) {
            calendar.set(Calendar.HOUR_OF_DAY, 9);
            calendar.set(Calendar.MINUTE, 0);
        }
        return calendar.getTimeInMillis();
    }
}
