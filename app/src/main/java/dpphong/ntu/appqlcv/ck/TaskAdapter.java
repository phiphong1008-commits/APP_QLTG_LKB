package dpphong.ntu.appqlcv.ck;

import android.app.DatePickerDialog;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    public void updateList(List<Task> newList) {
        taskList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.tvTitle.setText(task.getTitle() != null ? task.getTitle() : "Không có tiêu đề");

        String timeStr = task.getTime() != null ? task.getTime().trim() : "";
        String descStr = task.getDescription() != null ? task.getDescription().trim() : "";

        int timeVisibility = timeStr.isEmpty() ? View.GONE : View.VISIBLE;
        holder.tvTime.setVisibility(timeVisibility);
        holder.ivTimeIcon.setVisibility(timeVisibility);
        if (!timeStr.isEmpty()) {
            holder.tvTime.setText(timeStr);
        }

        holder.tvDesc.setText(descStr);
        holder.tvDesc.setVisibility(descStr.isEmpty() ? View.GONE : View.VISIBLE);

        if (task.getIcon() != null) {
            switch (task.getIcon()) {
                case "study":
                    holder.ivIcon.setImageResource(R.drawable.ic_book);
                    break;
                case "home":
                    holder.ivIcon.setImageResource(R.drawable.ic_home);
                    break;
                case "event":
                    holder.ivIcon.setImageResource(R.drawable.ic_event);
                    break;
                default:
                    holder.ivIcon.setImageResource(R.drawable.ic_work);
                    break;
            }
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_work);
        }

        String priority = task.getPriority() != null ? task.getPriority() : "Vừa";
        switch (priority) {
            case "Cao":
                holder.tvPriority.setText("Cao");
                holder.tvPriority.setBackgroundResource(R.drawable.bg_pill_high);
                holder.tvPriority.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.priorityHigh));
                holder.viewIndicator.setBackgroundResource(R.drawable.bg_pill_high);
                break;
            case "Thấp":
                holder.tvPriority.setText("Thấp");
                holder.tvPriority.setBackgroundResource(R.drawable.bg_pill_low);
                holder.tvPriority.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.priorityLow));
                holder.viewIndicator.setBackgroundResource(R.drawable.bg_pill_low);
                break;
            default:
                holder.tvPriority.setText("Vừa");
                holder.tvPriority.setBackgroundResource(R.drawable.bg_pill_medium);
                holder.tvPriority.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.priorityMedium));
                holder.viewIndicator.setBackgroundResource(R.drawable.bg_pill_medium);
                break;
        }

        bindExtras(holder, task);

        holder.cbStatus.setOnCheckedChangeListener(null);
        holder.cbStatus.setChecked(task.isCompleted());
        applyCompletedStyle(holder, task.isCompleted());

        holder.cardContainer.setOnLongClickListener(v -> {
            String[] actions = {"Chỉnh sửa", "Dời sang ngày khác", "Bắt đầu tập trung",
                    "Thêm vào Google Calendar", "Chia sẻ công việc", "Lưu thành mẫu", "Xóa công việc"};
            new MaterialAlertDialogBuilder(v.getContext())
                    .setTitle(task.getTitle())
                    .setItems(actions, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                openEditor(v, task);
                                break;
                            case 1:
                                openMoveDateDialog(v, task);
                                break;
                            case 2:
                                startFocus(v, task);
                                break;
                            case 3:
                                CalendarHelper.addToCalendar(v.getContext(), task);
                                break;
                            case 4:
                                CalendarHelper.shareTask(v.getContext(), task);
                                break;
                            case 5:
                                saveAsTemplate(v, task);
                                break;
                            default:
                                confirmDelete(v, task);
                                break;
                        }
                    })
                    .show();
            return true;
        });

        holder.cardContainer.setOnClickListener(v -> openEditor(v, task));

        holder.layoutTaskSubtasks.setOnClickListener(v -> showSubtaskDialog(holder, task));

        holder.cbStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applyCompletedStyle(holder, isChecked);
            task.setCompleted(isChecked);
            task.setCompletedAt(isChecked ? System.currentTimeMillis() : 0L);

            if (task.getId() != null) {
                HashMap<String, Object> updates = new HashMap<>();
                updates.put("isCompleted", isChecked);
                updates.put("completedAt", task.getCompletedAt());

                FirebaseDatabase.getInstance()
                        .getReference("Tasks")
                        .child(task.getId())
                        .updateChildren(updates)
                        .addOnFailureListener(e -> {
                            holder.cbStatus.setChecked(!isChecked);
                            task.setCompleted(!isChecked);
                            task.setCompletedAt(0L);
                            applyCompletedStyle(holder, !isChecked);
                            Log.e("TaskAdapter", "Lỗi update Firebase: " + e.getMessage());
                        });

                if (isChecked) {
                    spawnNextOccurrence(task);
                } else if (task.isNextSpawned() && !"none".equals(task.getRecurrence())) {
                    // Khi bỏ đánh dấu, cho phép lần hoàn thành sau tạo lại việc kế tiếp
                    task.setNextSpawned(false);
                    FirebaseDatabase.getInstance()
                            .getReference("Tasks")
                            .child(task.getId())
                            .child("nextSpawned")
                            .setValue(false);
                }
            }
        });
    }

    /**
     * Hiển thị danh sách, tiến độ việc con và huy hiệu lặp lại.
     */
    private void bindExtras(TaskViewHolder holder, Task task) {
        boolean hasList = !TextUtils.isEmpty(task.getListName());
        holder.tvTaskList.setVisibility(hasList ? View.VISIBLE : View.GONE);
        if (hasList) {
            holder.tvTaskList.setText(task.getListName());
        }

        List<Subtask> subtasks = task.getSubtasks();
        int subtaskTotal = subtasks != null ? subtasks.size() : 0;
        int subtaskDone = 0;
        if (subtasks != null) {
            for (Subtask subtask : subtasks) {
                if (subtask != null && subtask.isCompleted()) subtaskDone++;
            }
        }

        boolean hasSubtasks = subtaskTotal > 0;
        holder.layoutTaskSubtasks.setVisibility(hasSubtasks ? View.VISIBLE : View.GONE);
        if (hasSubtasks) {
            holder.tvTaskSubtasks.setText(subtaskDone + "/" + subtaskTotal);
        }

        boolean recurring = task.getRecurrence() != null && !"none".equals(task.getRecurrence());
        holder.ivTaskRecurrence.setVisibility(recurring ? View.VISIBLE : View.GONE);

        holder.layoutTaskBadges.setVisibility(
                (hasList || hasSubtasks || recurring) ? View.VISIBLE : View.GONE);
    }

    /**
     * Hộp thoại đánh dấu các việc con đã làm xong.
     */
    private void showSubtaskDialog(TaskViewHolder holder, Task task) {
        List<Subtask> subtasks = task.getSubtasks();
        if (subtasks == null || subtasks.isEmpty()) return;

        String[] titles = new String[subtasks.size()];
        boolean[] checked = new boolean[subtasks.size()];
        final boolean[] original = new boolean[subtasks.size()];
        for (int i = 0; i < subtasks.size(); i++) {
            Subtask subtask = subtasks.get(i);
            titles[i] = subtask != null && subtask.getTitle() != null ? subtask.getTitle() : "Việc con";
            checked[i] = subtask != null && subtask.isCompleted();
            original[i] = checked[i];
        }

        new MaterialAlertDialogBuilder(holder.itemView.getContext())
                .setTitle("Việc con")
                .setMultiChoiceItems(titles, checked, (dialog, which, isChecked) -> {
                    if (subtasks.get(which) != null) {
                        subtasks.get(which).setCompleted(isChecked);
                    }
                })
                .setPositiveButton("Lưu", (dialog, which) -> {
                    if (task.getId() != null) {
                        FirebaseDatabase.getInstance()
                                .getReference("Tasks")
                                .child(task.getId())
                                .child("subtasks")
                                .setValue(subtasks);
                    }
                    refreshHolder(holder);
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    for (int i = 0; i < subtasks.size() && i < original.length; i++) {
                        if (subtasks.get(i) != null) {
                            subtasks.get(i).setCompleted(original[i]);
                        }
                    }
                    refreshHolder(holder);
                })
                .show();
    }

    private void refreshHolder(TaskViewHolder holder) {
        int position = holder.getAdapterPosition();
        if (position != RecyclerView.NO_POSITION) {
            notifyItemChanged(position);
        }
    }

    private void openEditor(View v, Task task) {
        AddTodoFragment editFragment = new AddTodoFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("TASK_EDIT", task);
        editFragment.setArguments(bundle);

        AppCompatActivity activity = (AppCompatActivity) v.getContext();
        activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.calendar_container, editFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Dời công việc sang ngày khác và đặt lại nhắc nhở theo ngày mới.
     */
    private void openMoveDateDialog(View v, Task task) {
        Calendar calendar = Calendar.getInstance();
        try {
            String date = task.getDate();
            if (date != null && !date.isEmpty()) {
                java.util.Date parsed = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date);
                if (parsed != null) calendar.setTime(parsed);
            }
        } catch (Exception ignored) {
        }

        new DatePickerDialog(v.getContext(), (picker, year, month, dayOfMonth) -> {
            Calendar target = Calendar.getInstance();
            target.set(year, month, dayOfMonth);
            String newDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(target.getTime());

            HashMap<String, Object> update = new HashMap<>();
            update.put("date", newDate);

            FirebaseDatabase.getInstance().getReference("Tasks")
                    .child(task.getId())
                    .updateChildren(update)
                    .addOnSuccessListener(aVoid -> {
                        task.setDate(newDate);
                        ContextCompat.getMainExecutor(v.getContext()).execute(() -> {
                            Toast.makeText(v.getContext(), "Đã dời sang " + newDate,
                                    Toast.LENGTH_SHORT).show();
                            refreshHolderByView(v);
                        });

                        if (!TextUtils.isEmpty(task.getTime())) {
                            ReminderScheduler.cancel(v.getContext(), task.getId());
                            if (task.getReminderOffsets() != null) {
                                for (Long offset : task.getReminderOffsets()) {
                                    if (offset == null) continue;
                                    ReminderScheduler.schedule(v.getContext(), task.getId(),
                                            task.getTitle(), task.getDescription(), newDate,
                                            task.getTime(), offset);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(v.getContext(),
                            "Không dời được: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void startFocus(View v, Task task) {
        FocusFragment focusFragment = new FocusFragment();
        Bundle args = new Bundle();
        args.putString("TASK_ID", task.getId());
        args.putString("TASK_TITLE", task.getTitle());
        focusFragment.setArguments(args);

        AppCompatActivity activity = (AppCompatActivity) v.getContext();
        activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.calendar_container, focusFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Lưu cấu hình công việc thành mẫu để tái sử dụng.
     */
    private void saveAsTemplate(View v, Task task) {
        EditText input = new EditText(v.getContext());
        input.setHint("Tên mẫu");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(task.getTitle() != null ? task.getTitle() : "Mẫu công việc");
        int padding = Math.round(20 * v.getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        LinearLayout container = new LinearLayout(v.getContext());
        container.setPadding(padding, 0, padding, 0);
        container.addView(input);

        new MaterialAlertDialogBuilder(v.getContext())
                .setTitle("Lưu thành mẫu")
                .setMessage("Bạn có thể dùng lại mẫu này khi tạo công việc mới.")
                .setView(container)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) name = task.getTitle();
                    if (name == null || name.isEmpty()) name = "Mẫu công việc";
                    final String templateName = name;

                    String templateId = FirebaseDatabase.getInstance()
                            .getReference("Templates").push().getKey();
                    if (templateId == null) return;

                    TaskTemplate template = new TaskTemplate();
                    template.setId(templateId);
                    template.setUserId(task.getUserId());
                    template.setName(templateName);
                    template.setTitle(task.getTitle());
                    template.setDescription(task.getDescription());
                    template.setPriority(task.getPriority());
                    template.setIcon(task.getIcon());
                    template.setRecurrence(task.getRecurrence());
                    template.setListId(task.getListId());
                    template.setListName(task.getListName());
                    template.setListColor(task.getListColor());
                    template.setCreatedAt(System.currentTimeMillis());

                    FirebaseDatabase.getInstance().getReference("Templates")
                            .child(templateId).setValue(template)
                            .addOnSuccessListener(aVoid -> Toast.makeText(v.getContext(),
                                    "Đã lưu mẫu: " + templateName, Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void refreshHolderByView(View v) {
        // Danh sách sẽ tự cập nhật khi Firebase trả dữ liệu mới (ValueEventListener).
    }

    private void confirmDelete(View v, Task task) {
        new MaterialAlertDialogBuilder(v.getContext())
                .setTitle("Xóa công việc")
                .setMessage("Bạn có chắc chắn muốn xóa công việc '" + task.getTitle() + "' không?")
                .setIcon(R.drawable.ic_delete)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    if (task.getId() != null) {
                        ReminderScheduler.cancel(v.getContext(), task.getId());
                        FirebaseDatabase.getInstance()
                                .getReference("Tasks")
                                .child(task.getId())
                                .removeValue();
                    }
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Khi hoàn thành một việc lặp lại, tạo bản ghi cho kỳ kế tiếp.
     */
    private void spawnNextOccurrence(Task task) {
        if (task.getId() == null || task.isNextSpawned()) return;
        String recurrence = task.getRecurrence();
        if (recurrence == null || "none".equals(recurrence)) return;

        String nextDate = calculateNextDate(task.getDate(), recurrence);
        if (nextDate == null) return;

        // Chặn tạo trùng nếu bấm nhanh nhiều lần
        task.setNextSpawned(true);
        FirebaseDatabase.getInstance()
                .getReference("Tasks")
                .child(task.getId())
                .child("nextSpawned")
                .setValue(true);

        String newId = FirebaseDatabase.getInstance().getReference("Tasks").push().getKey();
        if (newId == null) return;

        HashMap<String, Object> clone = new HashMap<>();
        clone.put("id", newId);
        clone.put("userId", task.getUserId());
        clone.put("title", task.getTitle());
        clone.put("description", task.getDescription());
        clone.put("date", nextDate);
        clone.put("time", task.getTime());
        clone.put("priority", task.getPriority());
        clone.put("icon", task.getIcon());
        clone.put("isCompleted", false);
        clone.put("timestamp", System.currentTimeMillis());
        clone.put("listId", task.getListId());
        clone.put("listName", task.getListName());
        clone.put("listColor", task.getListColor());
        clone.put("recurrence", recurrence);
        clone.put("reminderOffsets", task.getReminderOffsets());
        clone.put("subtasks", resetSubtasks(task.getSubtasks()));
        clone.put("nextSpawned", false);

        FirebaseDatabase.getInstance()
                .getReference("Tasks")
                .child(newId)
                .setValue(clone);

        ReminderScheduler.cancel(holderContext(), newId);
        if (task.getReminderOffsets() != null) {
            for (Long offset : task.getReminderOffsets()) {
                if (offset == null) continue;
                ReminderScheduler.schedule(holderContext(), newId, task.getTitle(), task.getDescription(),
                        nextDate, task.getTime(), offset);
            }
        }
    }

    private android.content.Context holderContext() {
        return FirebaseDatabase.getInstance().getApp().getApplicationContext();
    }

    private List<Subtask> resetSubtasks(List<Subtask> original) {
        List<Subtask> copy = new ArrayList<>();
        if (original == null) return copy;
        for (Subtask subtask : original) {
            if (subtask == null) continue;
            copy.add(new Subtask(subtask.getId(), subtask.getTitle(), false));
        }
        return copy;
    }

    private String calculateNextDate(String date, String recurrence) {
        if (date == null || date.isEmpty()) return null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            java.util.Date parsed = sdf.parse(date);
            if (parsed == null) return null;

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parsed);
            switch (recurrence) {
                case "daily":
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    break;
                case "weekly":
                    calendar.add(Calendar.WEEK_OF_YEAR, 1);
                    break;
                case "monthly":
                    calendar.add(Calendar.MONTH, 1);
                    break;
                default:
                    return null;
            }
            return sdf.format(calendar.getTime());
        } catch (Exception e) {
            return null;
        }
    }

    private void applyCompletedStyle(TaskViewHolder holder, boolean completed) {
        if (completed) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setAlpha(0.5f);
            holder.tvDesc.setAlpha(0.45f);
            holder.ivIcon.setAlpha(0.45f);
            holder.tvTime.setAlpha(0.5f);
            holder.tvPriority.setAlpha(0.6f);
            holder.layoutTaskBadges.setAlpha(0.6f);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvTitle.setAlpha(1f);
            holder.tvDesc.setAlpha(1f);
            holder.ivIcon.setAlpha(1f);
            holder.tvTime.setAlpha(1f);
            holder.tvPriority.setAlpha(1f);
            holder.layoutTaskBadges.setAlpha(1f);
        }
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        CardView cardContainer;
        View viewIndicator;
        ImageView ivIcon, ivTimeIcon, ivTaskRecurrence;
        TextView tvTitle, tvDesc, tvTime, tvPriority, tvTaskList, tvTaskSubtasks;
        View layoutTaskBadges, layoutTaskSubtasks;
        CheckBox cbStatus;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContainer = itemView.findViewById(R.id.card_task_container);
            viewIndicator = itemView.findViewById(R.id.view_priority_indicator);
            ivIcon = itemView.findViewById(R.id.iv_task_icon);
            ivTimeIcon = itemView.findViewById(R.id.iv_task_time_icon);
            ivTaskRecurrence = itemView.findViewById(R.id.iv_task_recurrence);
            tvTitle = itemView.findViewById(R.id.tv_task_title);
            tvDesc = itemView.findViewById(R.id.tv_task_desc);
            tvTime = itemView.findViewById(R.id.tv_task_time);
            tvPriority = itemView.findViewById(R.id.tv_task_priority);
            tvTaskList = itemView.findViewById(R.id.tv_task_list);
            tvTaskSubtasks = itemView.findViewById(R.id.tv_task_subtasks);
            layoutTaskBadges = itemView.findViewById(R.id.layout_task_badges);
            layoutTaskSubtasks = itemView.findViewById(R.id.layout_task_subtasks);
            cbStatus = itemView.findViewById(R.id.cb_task_status);
        }
    }
}
