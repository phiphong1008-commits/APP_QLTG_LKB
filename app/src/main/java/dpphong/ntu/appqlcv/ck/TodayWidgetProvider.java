package dpphong.ntu.appqlcv.ck;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

/**
 * Widget hiển thị nhanh các việc hôm nay. Dữ liệu lấy từ bộ nhớ đệm
 * do TodoFragment cập nhật, không cần mở ứng dụng.
 */
public class TodayWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, buildViews(context));
        }
    }

    public static void updateWidget(Context context) {
        if (context == null) return;

        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName component = new ComponentName(context, TodayWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(component);
        for (int id : ids) {
            manager.updateAppWidget(id, buildViews(context));
        }
    }

    private static RemoteViews buildViews(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_today);

        String progress = TaskCache.getProgress(context);
        String lines = TaskCache.getLines(context);

        views.setTextViewText(R.id.tv_widget_progress, progress);
        views.removeAllViews(R.id.container_widget_tasks);

        int shown = 0;
        if (lines != null && !lines.isEmpty()) {
            for (String line : lines.split("\n")) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split("\\|", 3);
                boolean done = parts.length > 0 && "1".equals(parts[0]);
                String time = parts.length > 1 ? parts[1] : "";
                String title = parts.length > 2 ? parts[2] : "";

                String prefix = done ? "✓ " : "• ";
                String text = prefix + (time.isEmpty() ? "" : time + "  ") + title;

                RemoteViews row = new RemoteViews(context.getPackageName(), R.layout.widget_task_row);
                row.setTextViewText(R.id.tv_widget_row, text);
                row.setTextColor(R.id.tv_widget_row,
                        context.getColor(done ? R.color.colorTextHint : R.color.colorTextPrimary));
                views.addView(R.id.container_widget_tasks, row);

                shown++;
            }
        }

        views.setViewVisibility(R.id.tv_widget_empty, shown == 0 ? View.VISIBLE : View.GONE);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.container_widget_tasks, pendingIntent);
        views.setOnClickPendingIntent(R.id.tv_widget_progress, pendingIntent);

        return views;
    }
}
