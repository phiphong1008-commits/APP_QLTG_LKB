package dpphong.ntu.appqlcv.ck;

public class Task {
    private String title;
    private String description;
    private String date; // Định dạng yyyy-MM-dd
    private String priority; // "Cao", "Vừa", "Thấp"
    private int iconResId;

    public Task(String title, String description, String date, String priority, int iconResId) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.priority = priority;
        this.iconResId = iconResId;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getPriority() { return priority; }
    public int getIconResId() { return iconResId; }
}