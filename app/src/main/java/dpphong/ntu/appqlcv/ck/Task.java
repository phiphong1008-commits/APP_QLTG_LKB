package dpphong.ntu.appqlcv.ck;

import com.google.firebase.database.PropertyName;

import java.io.Serializable;

public class Task implements Serializable {
    private String id;
    private String title;
    private String description;
    private String date;
    private String time;
    private String priority;
    private boolean isCompleted;
    private long timestamp;
    private String userId;

    // BẮT BUỘC: Constructor rỗng dành cho Firebase
    public Task() {
    }

    public Task(String id, String title, String description, String date, String time, String priority, boolean isCompleted, long timestamp, String userId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.priority = priority;
        this.isCompleted = isCompleted;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    // --- CÁC HÀM GETTER VÀ SETTER ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }



    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    @PropertyName("isCompleted")
    public boolean isCompleted() {
        return isCompleted;
    }

    @PropertyName("isCompleted")
    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}