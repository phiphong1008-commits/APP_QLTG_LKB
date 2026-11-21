package dpphong.ntu.appqlcv.ck;

import java.io.Serializable;

/**
 * Một việc con nằm trong công việc chính.
 */
public class Subtask implements Serializable {
    private String id;
    private String title;
    private boolean completed;

    public Subtask() {
    }

    public Subtask(String id, String title, boolean completed) {
        this.id = id;
        this.title = title;
        this.completed = completed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
