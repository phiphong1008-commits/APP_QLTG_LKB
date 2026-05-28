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
import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    // Cập nhật lại danh sách khi người dùng chọn ngày khác
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

        holder.tvTitle.setText(task.getTitle());
        holder.tvDesc.setText(task.getDescription());
        holder.ivIcon.setImageResource(task.getIconResId());

        // Đổi màu background theo mức độ ưu tiên
        switch (task.getPriority()) {
            case "Cao":
                holder.cardContainer.setCardBackgroundColor(Color.parseColor("#FFCDD2")); // Đỏ nhạt
                break;
            case "Vừa":
                holder.cardContainer.setCardBackgroundColor(Color.parseColor("#FFF9C4")); // Vàng nhạt
                break;
            case "Thấp":
                holder.cardContainer.setCardBackgroundColor(Color.parseColor("#C8E6C9")); // Xanh lá nhạt
                break;
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