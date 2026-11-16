package dpphong.ntu.appqlcv.ck;

import java.io.Serializable;

/**
 * Danh sách / dự án do người dùng tạo (Học tập, Công việc, Cá nhân...).
 */
public class TaskList implements Serializable {
    private String id;
    private String name;
    private String color;
    private String userId;
    private long timestamp;

    public TaskList() {
    }

    public TaskList(String id, String name, String color, String userId, long timestamp) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
