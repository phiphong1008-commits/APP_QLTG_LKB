package dpphong.ntu.appqlcv.ck;

import com.google.firebase.database.PropertyName;

import java.io.Serializable;
import java.util.List;

public class Task implements Serializable {
    private String id;
    private String title;
    private String description;
    private String date;
    private String time;
    private String priority;
    private boolean isCompleted;
    private String icon;
    private long timestamp;
    private String userId;

    // P0: danh sách, lặp lại, nhắc nhiều mốc, việc con
    private String listId;
    private String listName;
    private String listColor;
    private String recurrence = "none";
    private List<Long> reminderOffsets;
    private List<Subtask> subtasks;
    private boolean nextSpawned;

    // BẮT BUỘC: Constructor rỗng dành cho Firebase
    public Task() {
    }

    public Task(String id, String title, String description, String date, String time, String priority,
                String icon, boolean isCompleted, long timestamp, String userId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.priority = priority;
        this.isCompleted = isCompleted;
        this.timestamp = timestamp;
        this.userId = userId;
        this.icon = icon;
    }

    // --- CÁC HÀM GETTER VÀ SETTER ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

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

    public String getListId() { return listId; }
    public void setListId(String listId) { this.listId = listId; }

    public String getListName() { return listName; }
    public void setListName(String listName) { this.listName = listName; }

    public String getListColor() { return listColor; }
    public void setListColor(String listColor) { this.listColor = listColor; }

    public String getRecurrence() { return recurrence; }
    public void setRecurrence(String recurrence) { this.recurrence = recurrence; }

    public List<Long> getReminderOffsets() { return reminderOffsets; }
    public void setReminderOffsets(List<Long> reminderOffsets) { this.reminderOffsets = reminderOffsets; }

    public List<Subtask> getSubtasks() { return subtasks; }
    public void setSubtasks(List<Subtask> subtasks) { this.subtasks = subtasks; }

    public boolean isNextSpawned() { return nextSpawned; }
    public void setNextSpawned(boolean nextSpawned) { this.nextSpawned = nextSpawned; }

    @PropertyName("isCompleted")
    public boolean isCompleted() {
        return isCompleted;
    }

    @PropertyName("isCompleted")
    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
