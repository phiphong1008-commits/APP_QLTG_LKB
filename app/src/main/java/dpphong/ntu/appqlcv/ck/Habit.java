package dpphong.ntu.appqlcv.ck;

import java.io.Serializable;

/**
 * Thói quen hằng ngày. Lịch sử hoàn thành lưu ở node HabitLogs/{habitId}/{yyyy-MM-dd}.
 */
public class Habit implements Serializable {
    private String id;
    private String userId;
    private String name;
    private String emoji;
    private String color;
    private long createdAt;

    public Habit() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
