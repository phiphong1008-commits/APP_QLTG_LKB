package dpphong.ntu.appqlcv.ck;

import java.io.Serializable;

/**
 * Một phiên tập trung (Pomodoro) được lưu vào Firebase để thống kê thời gian.
 */
public class FocusSession implements Serializable {
    private String id;
    private String userId;
    private String taskId;
    private String taskTitle;
    private int minutes;
    private long startedAt;
    private long createdAt;

    public FocusSession() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getTaskTitle() { return taskTitle; }
    public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }

    public int getMinutes() { return minutes; }
    public void setMinutes(int minutes) { this.minutes = minutes; }

    public long getStartedAt() { return startedAt; }
    public void setStartedAt(long startedAt) { this.startedAt = startedAt; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
