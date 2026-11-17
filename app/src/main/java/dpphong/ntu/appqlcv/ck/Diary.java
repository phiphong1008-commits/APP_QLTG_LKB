package dpphong.ntu.appqlcv.ck;

import java.io.Serializable;

public class Diary implements Serializable {
    private String id;
    private String title;
    private String content;
    private String date;
    private long timestamp;
    private String userId;

    // P1: nhật ký nâng cao
    private String mood;
    private String weather;
    private String prompt;
    private String photoBase64;
    private boolean pinned;

    public Diary() {
        // Bắt buộc cho Firebase
    }

    public Diary(String id, String title, String content, String date, long timestamp, String userId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    // --- GETTER VÀ SETTER ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }

    public String getWeather() { return weather; }
    public void setWeather(String weather) { this.weather = weather; }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public String getPhotoBase64() { return photoBase64; }
    public void setPhotoBase64(String photoBase64) { this.photoBase64 = photoBase64; }

    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }
}
