package dpphong.ntu.appqlcv.ck;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Lưu các tùy chọn giao diện: chế độ tối và cỡ chữ.
 */
public final class Prefs {
    private static final String NAME = "app_settings";
    private static final String KEY_NIGHT_MODE = "night_mode";
    private static final String KEY_FONT_SCALE = "font_scale";
    private static final String KEY_DIARY_PIN = "diary_pin";
    private static final String KEY_AI_TONE = "ai_tone";

    private Prefs() {
    }

    public static int getNightMode(Context context) {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .getInt(KEY_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public static void setNightMode(Context context, int mode) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .edit().putInt(KEY_NIGHT_MODE, mode).apply();
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public static float getFontScale(Context context) {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .getFloat(KEY_FONT_SCALE, 1f);
    }

    public static void setFontScale(Context context, float scale) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .edit().putFloat(KEY_FONT_SCALE, scale).apply();
    }

    // ===== Khóa nhật ký bằng PIN =====
    public static boolean hasDiaryPin(Context context) {
        String pin = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .getString(KEY_DIARY_PIN, null);
        return pin != null && !pin.isEmpty();
    }

    public static boolean checkDiaryPin(Context context, String pin) {
        String saved = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .getString(KEY_DIARY_PIN, null);
        return saved != null && saved.equals(pin);
    }

    public static void setDiaryPin(Context context, String pin) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_DIARY_PIN, pin).apply();
    }

    public static void clearDiaryPin(Context context) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .edit().remove(KEY_DIARY_PIN).apply();
    }

    public static String getAiTone(Context context) {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .getString(KEY_AI_TONE, "friendly");
    }

    public static void setAiTone(Context context, String tone) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_AI_TONE, tone).apply();
    }
}
