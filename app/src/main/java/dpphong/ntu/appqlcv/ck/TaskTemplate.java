package dpphong.ntu.appqlcv.ck;

import java.io.Serializable;

/**
 * Mẫu công việc dùng lại nhanh (P2).
 */
public class TaskTemplate implements Serializable {
    private String id;
    private String userId;
    private String name;
    private String title;
    private String description;
    private String priority;
    private String icon;
    private String recurrence;
    private String listId;
    private String listName;
    private String listColor;
    private long createdAt;

    public TaskTemplate() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getRecurrence() { return recurrence; }
    public void setRecurrence(String recurrence) { this.recurrence = recurrence; }

    public String getListId() { return listId; }
    public void setListId(String listId) { this.listId = listId; }

    public String getListName() { return listName; }
    public void setListName(String listName) { this.listName = listName; }

    public String getListColor() { return listColor; }
    public void setListColor(String listColor) { this.listColor = listColor; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
