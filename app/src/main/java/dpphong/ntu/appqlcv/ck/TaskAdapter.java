package dpphong.ntu.appqlcv.ck;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

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

        // Set Tiêu đề
        holder.tvTitle.setText(task.getTitle() != null ? task.getTitle() : "Không có tiêu đề");

        // Nối thêm Giờ (time) vào phần mô tả cho trực quan
        String timeStr = task.getTime() != null ? task.getTime() : "";
        String descStr = task.getDescription() != null ? task.getDescription() : "";
        holder.tvDesc.setText(timeStr + " - " + descStr);

        // Đổi Icon theo trạng thái hoàn thành (isCompleted) ban đầu khi tải dữ liệu
        if (task.isCompleted()) {
            holder.ivIcon.setImageResource(android.R.drawable.checkbox_on_background);
        } else {
            holder.ivIcon.setImageResource(android.R.drawable.ic_menu_agenda); // Icon mặc định
        }

        // --- ĐOẠN MỚI THÊM: BẮT SỰ KIỆN CLICK VÀO ICON ---
        holder.ivIcon.setOnClickListener(v -> {
            // 1. Đảo ngược trạng thái hiện tại (Đang true thì thành false, đang false thì thành true)
            boolean newStatus = !task.isCompleted();
            task.setCompleted(newStatus);

            // 2. Cập nhật lại giao diện của đúng dòng (item) này ngay lập tức cho mượt
            notifyItemChanged(position);

            // 3. Đẩy trạng thái mới lên Firebase để lưu giữ
            if (task.getId() != null) {
                com.google.firebase.database.FirebaseDatabase.getInstance()
                        .getReference("Tasks")
                        .child(task.getId())          // Trỏ vào đúng ID của công việc đó
                        .child("isCompleted")         // Chỉ cập nhật đúng trường isCompleted
                        .setValue(newStatus);
            }
        });
        // ------------------------------------------------

        // Đổi màu background theo mức độ ưu tiên
        if (task.getPriority() != null) {
            switch (task.getPriority()) {
                case "Cao":
                    holder.cardContainer.setCardBackgroundColor(android.graphics.Color.parseColor("#FFCDD2")); // Đỏ nhạt
                    break;
                case "Vừa":
                    holder.cardContainer.setCardBackgroundColor(android.graphics.Color.parseColor("#FFF9C4")); // Vàng nhạt
                    break;
                case "Thấp":
                    holder.cardContainer.setCardBackgroundColor(android.graphics.Color.parseColor("#C8E6C9")); // Xanh lá nhạt
                    break;
                default:
                    holder.cardContainer.setCardBackgroundColor(android.graphics.Color.WHITE);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        CardView cardContainer;
        ImageView ivIcon;
        TextView tvTitle, tvDesc;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContainer = itemView.findViewById(R.id.card_task_container);
            ivIcon = itemView.findViewById(R.id.iv_task_icon);
            tvTitle = itemView.findViewById(R.id.tv_task_title);
            tvDesc = itemView.findViewById(R.id.tv_task_desc);
        }
    }
}