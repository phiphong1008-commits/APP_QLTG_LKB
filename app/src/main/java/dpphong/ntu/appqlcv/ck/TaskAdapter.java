package dpphong.ntu.appqlcv.ck;

import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

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

        // Giờ và mô tả hiển thị gọn gàng, ẩn nếu không có dữ liệu
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

        // Icon theo loại công việc
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

        // Màu nhấn và nhãn mức độ ưu tiên
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

        // Tắt Listener tạm thời để không bị lỗi gọi nhầm khi cuộn danh sách
        holder.cbStatus.setOnCheckedChangeListener(null);
        holder.cbStatus.setChecked(task.isCompleted());
        applyCompletedStyle(holder, task.isCompleted());

        // XỬ LÝ XÓA (NHẤN GIỮ)
        holder.cardContainer.setOnLongClickListener(v -> {
            new MaterialAlertDialogBuilder(v.getContext())
                    .setTitle("Xóa công việc")
                    .setMessage("Bạn có chắc chắn muốn xóa công việc '" + task.getTitle() + "' không?")
                    .setIcon(R.drawable.ic_delete)
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        if (task.getId() != null) {
                            com.google.firebase.database.FirebaseDatabase.getInstance()
                                    .getReference("Tasks")
                                    .child(task.getId())
                                    .removeValue();
                        }
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();
            return true;
        });

        // XỬ LÝ CHỈNH SỬA (NHẤN CHẠM)
        holder.cardContainer.setOnClickListener(v -> {
            AddTodoFragment editFragment = new AddTodoFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("TASK_EDIT", task);
            editFragment.setArguments(bundle);

            AppCompatActivity activity = (AppCompatActivity) v.getContext();
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.calendar_container, editFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Lắng nghe sự kiện khi người dùng bấm vào CheckBox
        holder.cbStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applyCompletedStyle(holder, isChecked);
            task.setCompleted(isChecked);

            if (task.getId() != null) {
                com.google.firebase.database.FirebaseDatabase.getInstance()
                        .getReference("Tasks")
                        .child(task.getId())
                        .child("isCompleted")
                        .setValue(isChecked)
                        .addOnFailureListener(e -> {
                            holder.cbStatus.setChecked(!isChecked);
                            task.setCompleted(!isChecked);
                            applyCompletedStyle(holder, !isChecked);
                            Log.e("TaskAdapter", "Lỗi update Firebase: " + e.getMessage());
                        });
            }
        });
    }

    private void applyCompletedStyle(TaskViewHolder holder, boolean completed) {
        if (completed) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setAlpha(0.5f);
            holder.tvDesc.setAlpha(0.45f);
            holder.ivIcon.setAlpha(0.45f);
            holder.tvTime.setAlpha(0.5f);
            holder.tvPriority.setAlpha(0.6f);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvTitle.setAlpha(1f);
            holder.tvDesc.setAlpha(1f);
            holder.ivIcon.setAlpha(1f);
            holder.tvTime.setAlpha(1f);
            holder.tvPriority.setAlpha(1f);
        }
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        CardView cardContainer;
        View viewIndicator;
        ImageView ivIcon, ivTimeIcon;
        TextView tvTitle, tvDesc, tvTime, tvPriority;
        CheckBox cbStatus;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContainer = itemView.findViewById(R.id.card_task_container);
            viewIndicator = itemView.findViewById(R.id.view_priority_indicator);
            ivIcon = itemView.findViewById(R.id.iv_task_icon);
            ivTimeIcon = itemView.findViewById(R.id.iv_task_time_icon);
            tvTitle = itemView.findViewById(R.id.tv_task_title);
            tvDesc = itemView.findViewById(R.id.tv_task_desc);
            tvTime = itemView.findViewById(R.id.tv_task_time);
            tvPriority = itemView.findViewById(R.id.tv_task_priority);
            cbStatus = itemView.findViewById(R.id.cb_task_status);
        }
    }
}
