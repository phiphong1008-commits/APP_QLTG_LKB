package dpphong.ntu.appqlcv.ck;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Nhắc công việc theo vị trí (P1).
 *
 * Vì không dùng geofencing của Play Services, app kiểm tra khoảng cách tới các
 * công việc có gắn tọa độ mỗi khi được mở. Nếu đang ở trong bán kính 250 m và
 * hôm nay chưa nhắc việc đó, một thông báo sẽ hiện ra.
 */
public final class LocationHelper {

    private static final String CHANNEL_ID = "LOCATION_ALERTS";
    private static final String PREFS = "location_reminders";
    private static final float RADIUS_METERS = 250f;

    private LocationHelper() {
    }

    public interface LocationCallback {
        void onLocation(double lat, double lng);

        void onError(String message);
    }

    public static boolean hasPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Lấy nhanh vị trí hiện tại: ưu tiên vị trí đã lưu, sau đó yêu cầu một lần cập nhật.
     */
    public static void requestSingleUpdate(Activity activity, LocationCallback callback) {
        if (!hasPermission(activity)) {
            callback.onError("Chưa được cấp quyền vị trí");
            return;
        }

        LocationManager manager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (manager == null) {
            callback.onError("Thiết bị không hỗ trợ vị trí");
            return;
        }

        Location last = bestLastKnown(manager);
        if (last != null) {
            callback.onLocation(last.getLatitude(), last.getLongitude());
        }

        String provider = null;
        try {
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                provider = LocationManager.GPS_PROVIDER;
            } else if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                provider = LocationManager.NETWORK_PROVIDER;
            }
        } catch (Exception ignored) {
        }

        if (provider == null) {
            if (last == null) callback.onError("Vui lòng bật GPS hoặc mạng để lấy vị trí");
            return;
        }

        try {
            manager.requestSingleUpdate(provider, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        callback.onLocation(location.getLatitude(), location.getLongitude());
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            }, Looper.getMainLooper());
        } catch (SecurityException e) {
            if (last == null) callback.onError("Không có quyền truy cập vị trí");
        } catch (Exception e) {
            if (last == null) callback.onError("Không lấy được vị trí: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra các công việc có gắn vị trí và gửi thông báo khi ở gần.
     */
    public static void checkNearbyTasks(Activity activity) {
        if (activity == null || !hasPermission(activity)) return;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        LocationManager manager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (manager == null) return;

        Location current = bestLastKnown(manager);
        if (current == null) return;

        final double lat = current.getLatitude();
        final double lng = current.getLongitude();

        Query query = FirebaseDatabase.getInstance().getReference("Tasks")
                .orderByChild("userId").equalTo(user.getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<NearbyTask> nearby = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Task task = child.getValue(Task.class);
                    if (task == null || task.isCompleted()) continue;
                    if (TextUtils.isEmpty(task.getId())) continue;
                    if (task.getLocationLat() == null || task.getLocationLng() == null) continue;
                    if (wasNotifiedToday(activity, task.getId())) continue;

                    float[] result = new float[1];
                    Location.distanceBetween(lat, lng,
                            task.getLocationLat(), task.getLocationLng(), result);
                    if (result[0] <= RADIUS_METERS) {
                        nearby.add(new NearbyTask(task, result[0]));
                    }
                }

                Collections.sort(nearby, (a, b) -> Float.compare(a.distance, b.distance));

                int limit = Math.min(3, nearby.size());
                for (int i = 0; i < limit; i++) {
                    NearbyTask item = nearby.get(i);
                    notifyNearby(activity, item.task);
                    markNotified(activity, item.task.getId());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private static Location bestLastKnown(LocationManager manager) {
        if (manager == null) return null;

        Location best = null;
        String[] providers = {
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
        };

        for (String provider : providers) {
            try {
                Location location = manager.getLastKnownLocation(provider);
                if (location == null) continue;
                if (best == null || location.getTime() > best.getTime()) {
                    best = location;
                }
            } catch (SecurityException ignored) {
            } catch (Exception ignored) {
            }
        }
        return best;
    }

    private static void notifyNearby(Context context, Task task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Nhắc theo vị trí", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Nhắc công việc khi bạn ở gần địa điểm đã chọn");
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                task.getId() != null ? task.getId().hashCode() : 0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? PendingIntent.FLAG_IMMUTABLE : 0));

        String place = !TextUtils.isEmpty(task.getLocationName())
                ? task.getLocationName() : "địa điểm đã chọn";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_event)
                .setContentTitle("Bạn đang ở gần " + place)
                .setContentText(task.getTitle() != null ? task.getTitle() : "Công việc cần làm")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        int id = task.getId() != null ? task.getId().hashCode() : (int) System.currentTimeMillis();
        manager.notify(id, builder.build());
    }

    private static boolean wasNotifiedToday(Context context, String taskId) {
        if (taskId == null) return false;
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String saved = prefs.getString(taskId, "");
        return today().equals(saved);
    }

    private static void markNotified(Context context, String taskId) {
        if (taskId == null) return;
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(taskId, today()).apply();
    }

    private static String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private static class NearbyTask {
        final Task task;
        final float distance;

        NearbyTask(Task task, float distance) {
            this.task = task;
            this.distance = distance;
        }
    }
}
