package dpphong.ntu.appqlcv.ck;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Lưu nhanh 5 việc hôm nay để widget hiển thị mà không cần đọc Firebase.
 */
public final class TaskCache {
    private static final String NAME = "widget_today_cache";
    private static final String KEY_LINES = "lines";
    private static final String KEY_COUNT = "count";

    private TaskCache() {
    }

    public static void saveToday(Context context, java.util.List<Task> tasks) {
        if (context == null || tasks == null) return;

        int completed = 0;
        StringBuilder builder = new StringBuilder();
        int shown = 0;
        for (Task task : tasks) {
            if (task.isCompleted()) completed++;
            if (shown >= 5) continue;
            String time = task.getTime() != null ? task.getTime() : "";
            String title = task.getTitle() != null ? task.getTitle() : "Công việc";
            builder.append(task.isCompleted() ? "1" : "0")
                    .append("|")
                    .append(time)
                    .append("|")
                    .append(title.replace("|", "/"))
                    .append("\n");
            shown++;
        }

        SharedPreferences prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_LINES, builder.toString())
                .putString(KEY_COUNT, completed + "/" + tasks.size())
                .apply();
    }

    public static String getProgress(Context context) {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .getString(KEY_COUNT, "0/0");
    }

    public static String getLines(Context context) {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .getString(KEY_LINES, "");
    }
}
