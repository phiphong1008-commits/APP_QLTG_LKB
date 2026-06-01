package dpphong.ntu.appqlcv.ck;

import android.graphics.Color;
import android.graphics.Paint; // Thêm thư viện Paint cho hiệu ứng gạch chữ
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox; // Thêm thư viện CheckBox
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

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

        // Giữ nguyên Icon mặc định cho đẹp (không đổi thành icon checkbox nữa)
        holder.ivIcon.setImageResource(android.R.drawable.ic_menu_agenda);

        // ========================================================
        // --- XỬ LÝ CHECKBOX VÀ TRẠNG THÁI (XONG/CHƯA XONG) ---
        // ========================================================

        // 1. Tắt Listener tạm thời để không bị lỗi gọi nhầm khi cuộn danh sách (Recycle)
        holder.cbStatus.setOnCheckedChangeListener(null);

        // 2. Cài đặt trạng thái CheckBox theo dữ liệu tải về
        holder.cbStatus.setChecked(task.isCompleted());

        // 3. Cài đặt hiệu ứng gạch ngang chữ ban đầu
        if (task.isCompleted()) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        // 4. Lắng nghe sự kiện khi người dùng tự tay bấm vào CheckBox
        holder.cbStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {

            // Cập nhật hiệu ứng gạch chữ ngay lập tức cho mượt
            if (isChecked) {
                holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }

            // Lưu trạng thái vào model cục bộ
            task.setCompleted(isChecked);

            // Đẩy trạng thái mới lên Firebase
            if (task.getId() != null) {
                com.google.firebase.database.FirebaseDatabase.getInstance()
                        .getReference("Tasks")
                        .child(task.getId())
                        .child("isCompleted")
                        .setValue(isChecked)
                        .addOnFailureListener(e -> {
                            // Nếu mạng lỗi hoặc lưu thất bại, hoàn tác lại UI
                            holder.cbStatus.setChecked(!isChecked);
                            task.setCompleted(!isChecked);
                            Log.e("TaskAdapter", "Lỗi update Firebase: " + e.getMessage());
                        });
            }
        });
        // ========================================================

        // Đổi màu background theo mức độ ưu tiên
        if (task.getPriority() != null) {
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
                default:
                    holder.cardContainer.setCardBackgroundColor(Color.WHITE);
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
        CheckBox cbStatus; // Khai báo CheckBox

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContainer = itemView.findViewById(R.id.card_task_container);
            ivIcon = itemView.findViewById(R.id.iv_task_icon);
            tvTitle = itemView.findViewById(R.id.tv_task_title);
            tvDesc = itemView.findViewById(R.id.tv_task_desc);
            cbStatus = itemView.findViewById(R.id.cb_task_status); // Ánh xạ CheckBox từ XML
        }
    }
}